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

case class FileSystemRepositoryResolver(rootPath: String) {
  def withRespositoryFor[T](id: String)(block: RepositoryResolver.RepositoryHandler[T]): Box[T] = {
    val compositeResult: Box[Box[T]] = for {
      fileHandle <- tryo(new File(s"$rootPath/$id")).filter(_.exists)
      createdRepository <- tryo(FileRepositoryBuilder.create(fileHandle))
      blockResult <- tryo(block(createdRepository))
      _ = createdRepository.close()
    } yield {
      blockResult
    }

    compositeResult match {
      case Full(blockResult) =>
        blockResult

      case forComprehensionFailure: EmptyBox =>
        forComprehensionFailure
    }
  }
}
