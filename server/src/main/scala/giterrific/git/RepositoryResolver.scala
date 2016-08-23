/**
 *
 * Copyright 2016 Matthew Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  def exists(path: String): Boolean = {
    new File(s"$rootPath/$path").exists()
  }

  def withRespositoryFor[T](id: String)(block: RepositoryResolver.RepositoryHandler[T]): Box[T] = {
    logger.trace(s"Resolving repository $rootPath/$id")
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
