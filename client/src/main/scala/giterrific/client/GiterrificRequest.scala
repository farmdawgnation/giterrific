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
package giterrific.client

import giterrific.core._
import dispatch._
import net.liftweb.json._
import net.liftweb.json.Extraction._
import scala.concurrent.ExecutionContext

class GiterrificRequesterBase(
  baseUrl: String,
  repoName: String
)(implicit ec: ExecutionContext) {
  def withRef(refName: String) = {
    new GiterrificRequester(
      baseUrl,
      repoName,
      refName,
      None
    )
  }
}

class GiterrificRequester(
  baseUrl: String,
  repoName: String,
  refName: String,
  path: Option[String]
)(implicit ec: ExecutionContext) {
  implicit val formats = DefaultFormats
  val baseRequestBuilder: Req = url(baseUrl) / "api" / "v1" / "repos" / repoName / "commits" / refName

  private[this] def run[T](request: Req)(implicit mf: Manifest[T]): Future[T] = {
    Http {
      baseRequestBuilder <:<
        Map("Content-Type" -> "application/json", "Accept" -> "application/json") OK
        as.String
    } .map { response =>
      parse(response).extract[T]
    }
  }

  def withPath(path: String): GiterrificRequester = {
    new GiterrificRequester(
      baseUrl,
      repoName,
      refName,
      Some(path)
    )
  }

  def getCommits(skip: Int = 0, maxCount: Int = 20): Future[List[RepositoryCommitSummary]] = {
    run(baseRequestBuilder <<? Map("skip" -> skip.toString, "maxCount" -> maxCount.toString))
  }

  def getTree(): Future[List[RepositoryFileSummary]] = {
    val treeRequestBuilder = if (path.isEmpty) {
      baseRequestBuilder / "tree"
    } else {
      baseRequestBuilder / "tree" / path.getOrElse("")
    }

    run(treeRequestBuilder)
  }

  def getContents(): Future[RepositoryFileContent] = {
    if (path.isEmpty) {
      Future.failed(new IllegalStateException("You can only request content if a path is specified."))
    } else {
      val contentRequestBuilder = baseRequestBuilder / "contents" / path.getOrElse("")

      run(contentRequestBuilder)
    }
  }
}
