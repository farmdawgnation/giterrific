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

  /**
   * Opens a managed RevWalk instance for a repositoy. Accepts a block that will
   * use the RevWalk instance. At the end of the block, the RevWalk intance is closed.
   *
   * @param repo The [[org.eclipse.jgit.lib.Repository]] to open the RevWalk against.
   * @param block The block that uses the RevWalk.
   */
  def withRevWalkFor[R](repo: Repository)(block: WrapperBlock[RevWalk, R]): Box[R] =
    withWalkFor[RevWalk, R](new RevWalk(repo))(block)

  /**
   * Opens a managed TreeWalk instance for a repository. Accepts a block that will
   * use the TreeWalk instance. At the end of the block, the TreeWalk instance is closed.
   *
   * @param repo The [[org.eclipse.jgit.lib.Repository]] to open the TreeWalk against.
   * @param block The block that uses the TreeWalk.
   */
  def withTreeWalkFor[R](repo: Repository)(block: WrapperBlock[TreeWalk, R]): Box[R] =
    withWalkFor[TreeWalk, R](new TreeWalk(repo))(block)

  /**
   * Retrieve a concrete Ref from a repository.
   *
   * @param repo The Repository to retrieve the ref from.
   * @param identifier The ref identifier to find: could be a sha or a branch name.
   */
  def getRef(repo: Repository, identifier: String): Box[Ref] = {
    flattenBoxes(tryo(Box.legacyNullTest(repo.findRef(identifier))))
  }

  /**
   * Retrieve a commit reference for a Ref.
   *
   * @param walker The RevWalk instance to retrieve the commit from.
   * @param ref The Ref referencing the commit.
   */
  def getCommit(walker: RevWalk, ref: Ref): Box[RevCommit] = {
    tryo(walker.parseCommit(ref.getObjectId()))
  }

  /**
   * Retrieves the tree for a commit. This should be used with a TreeWalk that will let you
   * walk that tree to explore its contents.
   *
   * @param commit The commit to pull the tree for.
   */
  def getCommitTree(commit: RevCommit): RevTree = {
    commit.getTree()
  }

  /**
   * Adds a tree to a tree walker.
   *
   * @param walker The tree walker that you'd like to use.
   * @param id Anything identifying the tree to add. Typically a RevTree instance.
   * @return The position of the added tree within the walker.
   */
  def addTree(walker: TreeWalk, id: AnyObjectId): Box[Int] = {
    tryo(walker.addTree(id))
  }

  /**
   * Browse to a specific path within a tree walker.
   *
   * @param walker The tree walker that you'd like to navigate in.
   * @param path The path you'd like to navigate the tree walker to.
   */
  def navigateTreeToPath(walker: TreeWalk, path: Seq[String]): Unit = {
    for (pathPart <- path) {
      walker.setFilter(PathFilter.create(pathPart))
      walker.next()
      walker.setFilter(null)
      walker.enterSubtree()
    }
  }

  /**
   * Set the tree walker to only display a file matching a particular name.
   *
   * @param walker The tree walker that you'd like to naviagte in.
   * @param fileName The file name you'd like to find.
   */
  def filterTreeToFile(walker: TreeWalk, fileName: String): Unit = {
    walker.setFilter(PathFilter.create(fileName))
    walker.next()
  }

  /**
   * Generate a commit summary for a repository. This method includes built in support for
   * pagination by way of "skip" and "maxCount".
   *
   * @param walker The revision walker that you'd like to use to generate the summary.
   * @param startCommit The RevCommit that you'd like the revision walker to start from.
   * @param skip The number of entries to skip.
   * @param maxCount The maximum number of entries to return.
   */
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

  /**
   * Generate a file summary of the contents of the tree walker. When a tree walker has been
   * navigated to a certain depth it only prepends the childen nodes in front of the parent
   * node's siblings. So in order to behave properly when navigating subfolders an expectedDepth
   * must be provided. Once the depth of the walker no longer matches that depth, the file summary
   * generation terminates.
   *
   * @param walker The tree walker to use.
   * @param expectedDepth The depth that you expect your files to be at.
   */
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

  /**
   * Retrieves a file content summary for the current file in the tree walker.
   *
   * This can only retrieve files up to a certain size. Past that size, you must retrieve the raw
   * stream to the file. The contents of the file are represented as a base64 encoded string.
   *
   * @param walker The tree walker currently queued up to the file you'd like to read.
   */
  def toFileContent(walker: TreeWalk): Box[RepositoryFileContent] = {
    withCloseable(walker.getObjectReader()) { objectReader =>
      val currentObjectId = walker.getObjectId(0)

      tryo(objectReader.open(currentObjectId)).flatMap { objectLoader =>
        if (objectLoader.isLarge) {
          Failure("Object is too large to retrieve a summary of. Please request the raw file.")
        } else {
          for {
            bytesOfObject <- tryo(objectLoader.getCachedBytes())
            base64OfObject = base64Encode(bytesOfObject)
          } yield {
            RepositoryFileContent(
              sha = walker.getNameString(),
              content = base64OfObject,
              encoding = "base64",
              size = objectLoader.getSize()
            )
          }
        }
      }
    }
  }
}
