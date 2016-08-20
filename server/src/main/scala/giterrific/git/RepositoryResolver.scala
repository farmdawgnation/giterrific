package giterrific.git

import java.io.File

import net.liftweb.common._
import net.liftweb.util.Helpers._
import org.eclipse.jgit.lib._
import org.eclipse.jgit.storage.file._

/**
 * A class that determines provides a concrete repository for some arbitrary identifier.
 */
sealed trait RepositoryResolver {
  def withRespositoryFor[T](id: String)(block: RepositoryResolver.RepositoryHandler[T]): Box[T]
}
object RepositoryResolver {
  type RepositoryHandler[T] = (Repository)=>Box[T]
}

case class FileSystemRepositoryResolver(rootPath: String) extends Loggable {
  def withRespositoryFor[T](id: String)(block: RepositoryResolver.RepositoryHandler[T]): Box[T] = {
    logger.debug(s"Resolving repository $rootPath/$id")
    val compositeResult: Box[Box[T]] = for {
      fileHandle <- tryo(new File(s"$rootPath/$id")).filter(_.exists)
      createdRepository <- tryo(FileRepositoryBuilder.create(fileHandle))
      _ = logger.debug(s"Resolution of $id successful.")
      blockResult <- tryo(block(createdRepository))
      _ = createdRepository.close()
    } yield {
      blockResult
    }

    compositeResult match {
      case Full(blockResult) =>
        logger.trace(s"Resolver block returned: $blockResult")
        blockResult

      case forComprehensionFailure: EmptyBox =>
        logger.debug(s"Issue locating repository: $forComprehensionFailure")
        forComprehensionFailure
    }
  }
}
