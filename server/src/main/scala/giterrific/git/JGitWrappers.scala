package giterrific.git

import java.lang.AutoCloseable
import scala.collection.JavaConversions._

import giterrific.core._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import org.eclipse.jgit.lib._
import org.eclipse.jgit.treewalk._
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
    tryo(repo.findRef(commitIdentifier))
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

  def toCommitSummary(walker: RevWalk, skip: Int, maxCount: Int): Seq[RepositoryCommitSummary] = {
    val revFilter = AndRevFilter.create(
      SkipRevFilter.create(skip),
      MaxCountRevFilter.create(maxCount)
    )

    walker.setRevFilter(revFilter)

    for (commit <- walker.toSeq) yield {
      val author = commit.getAuthorIdent()
      val committer = commit.getCommitterIdent()

      RepositoryCommitSummary(
        sha = commit.toObjectId.toString,
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
}
