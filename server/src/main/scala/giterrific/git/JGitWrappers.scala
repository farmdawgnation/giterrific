package giterrific.git

import java.lang.AutoCloseable
import scala.collection._
import scala.collection.JavaConversions._

import giterrific.core._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import org.eclipse.jgit.lib._
import org.eclipse.jgit.treewalk._
import org.eclipse.jgit.treewalk.filter._
import org.eclipse.jgit.revwalk._
import org.eclipse.jgit.revwalk.filter._

/**
 * JGit's API is kind of ugly by Scala standards - so we'll wrap its functionality
 * up in little composable functions. :)
 */
object JGitWrappers {
  type WrapperBlock[A, B] = (A)=>Box[B]

  private def flattenBoxes[T](boxes: Box[Box[T]]): Box[T] = {
    boxes match {
      case Full(x) =>
        x

      case e: EmptyBox =>
        e
    }
  }

  private def withCloseable[C <: AutoCloseable, R](closeable: C)(block: (C)=>R): R = {
    val result = block(closeable)
    closeable.close()
    result
  }

  private def withWalkFor[W <: AutoCloseable, R](walkMaker: => W)(block: WrapperBlock[W, R]): Box[R] = {
    val compositeResult = for {
      walker <- tryo(walkMaker)
      blockResult <- tryo(block(walker))
      _ = walker.close()
    } yield {
      blockResult
    }

    flattenBoxes(compositeResult)
  }

  def withRevWalkFor[R](repo: Repository)(block: WrapperBlock[RevWalk, R]): Box[R] =
    withWalkFor[RevWalk, R](new RevWalk(repo))(block)

  def withTreeWalkFor[R](repo: Repository)(block: WrapperBlock[TreeWalk, R]): Box[R] =
    withWalkFor[TreeWalk, R](new TreeWalk(repo))(block)

  def getRef(repo: Repository, commitIdentifier: String): Box[Ref] = {
    flattenBoxes(tryo(Box.legacyNullTest(repo.findRef(commitIdentifier))))
  }

  def getCommit(walker: RevWalk, ref: Ref): Box[RevCommit] = {
    tryo(walker.parseCommit(ref.getObjectId()))
  }

  def getCommitTree(commit: RevCommit): RevTree = {
    commit.getTree()
  }

  def addTree(walker: TreeWalk, id: AnyObjectId): Box[Int] = {
    tryo(walker.addTree(id))
  }

  def filterTreeByPath(walker: TreeWalk, path: Seq[String]): Unit = {
    for (pathPart <- path) {
      walker.setFilter(PathFilter.create(pathPart))
      walker.next()
      walker.setFilter(null)
      walker.enterSubtree()
    }
  }

  def filterTreeToFile(walker: TreeWalk, file: String): Unit = {
    walker.setFilter(PathFilter.create(file))
    walker.next()
  }

  def toCommitSummary(walker: RevWalk, startCommit: RevCommit, skip: Int, maxCount: Int): Seq[RepositoryCommitSummary] = {
    val revFilter = AndRevFilter.create(
      SkipRevFilter.create(skip),
      MaxCountRevFilter.create(maxCount)
    )

    walker.markStart(startCommit)
    walker.setRevFilter(revFilter)

    for (commit <- walker.iterator().toSeq) yield {
      val author = commit.getAuthorIdent()
      val committer = commit.getCommitterIdent()

      RepositoryCommitSummary(
        sha = commit.getName,
        author = RepositoryCommitIdentity(
          author.getWhen(),
          author.getName(),
          author.getEmailAddress()
        ),
        committer = RepositoryCommitIdentity(
          committer.getWhen(),
          committer.getName(),
          committer.getEmailAddress()
        ),
        message = commit.getFullMessage()
      )
    }
  }

  def toFileSummary(walker: TreeWalk, expectedDepth: Int): Seq[RepositoryFileSummary] = {
    withCloseable(walker.getObjectReader()) { objectReader =>
      var resultSeq = Seq[RepositoryFileSummary]()

      while (walker.next() && walker.getDepth() == expectedDepth) {
        val currentObjectId = walker.getObjectId(0)

        resultSeq = resultSeq :+ RepositoryFileSummary(
          sha = walker.getNameString(),
          path = walker.getPathString(),
          isDirectory = walker.isSubtree(),
          mode = walker.getFileMode().toString,
          size = objectReader.getObjectSize(currentObjectId, ObjectReader.OBJ_ANY)
        )
      }

      resultSeq
    }
  }

  def toFileContent(walker: TreeWalk): Box[RepositoryFileContent] = {
    withCloseable(walker.getObjectReader()) { objectReader =>
      val currentObjectId = walker.getObjectId(0)

      tryo(objectReader.open(currentObjectId)).flatMap { objectLoader =>
        val bytesOfObject = objectLoader.getCachedBytes()
        val base64OfObject = base64Encode(bytesOfObject)

        Full(RepositoryFileContent(
          sha = walker.getNameString(),
          content = base64OfObject,
          encoding = "base64",
          size = objectLoader.getSize()
        ))
      }
    }
  }
}
