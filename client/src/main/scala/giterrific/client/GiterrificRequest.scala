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
import giterrific.driver.http._
import net.liftweb.json._
import net.liftweb.json.Extraction._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Representation of a parial request to Giterrific. Specifically, one that has a base URL and a
 * repository, but does not yet have a ref associated with it.
 *
 * @param driver The HTTP Driver implementation in use by this class.
 * @param baseUrl The base URL used for the Giterrific server.
 * @param repoName The repository name this base refers to.
 */
class GiterrificRequesterBase[ReqType <: HttpReq[ReqType]](
  driver: HttpDriver[ReqType],
  baseUrl: String,
  repoName: String
)(implicit ec: ExecutionContext) {
  def withRef(refName: String) = {
    new GiterrificRequester(
      driver,
      baseUrl,
      repoName,
      refName,
      None
    )
  }
}

class GiterrificRequester[ReqType <: HttpReq[ReqType]](
  driver: HttpDriver[ReqType],
  baseUrl: String,
  repoName: String,
  refName: String,
  path: Option[String]
)(implicit ec: ExecutionContext) {
  implicit val formats = DefaultFormats

  val baseRequest: ReqType = driver.url(baseUrl) / "api" / "v1" / "repos" / repoName / "commits" / refName

  private[this] def run[T](request: ReqType)(implicit mf: Manifest[T]): Future[T] = {
    val requestWithContentHeaders = request.withHeaders(Map(
      "Content-Type" -> "application/json",
      "Accept" -> "application/json"
    ))

    driver.run(requestWithContentHeaders).map { response =>
      parse(response).extract[T]
    }
  }

  def withPath(path: String): GiterrificRequester[ReqType] = {
    new GiterrificRequester(
      driver,
      baseUrl,
      repoName,
      refName,
      Some(path)
    )
  }

  def getCommits(skip: Int = 0, maxCount: Int = 20): Future[List[RepositoryCommitSummary]] = {
    run[List[RepositoryCommitSummary]](baseRequest.withQuery(Map(
      "skip" -> skip.toString,
      "maxCount" -> maxCount.toString
    )))
  }

  def getTree(): Future[List[RepositoryFileSummary]] = {
    val treeRequestBuilder = if (path.isEmpty) {
      baseRequest / "tree"
    } else {
      baseRequest / "tree" / path.getOrElse("")
    }

    run[List[RepositoryFileSummary]](treeRequestBuilder)
  }

  def getContents(): Future[RepositoryFileContent] = {
    if (path.isEmpty) {
      Future.failed(new IllegalStateException("You can only request content if a path is specified."))
    } else {
      val contentRequestBuilder = baseRequest / "contents" / path.getOrElse("")

      run[RepositoryFileContent](contentRequestBuilder)
    }
  }
}
