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

import java.io.InputStream
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
  /**
   * The ref that you'd like to execute operations on.
   *
   * The validity of the ref isn't validated at this point.
   *
   * @param refName The name of the ref.
   */
  def withRef(refName: String): GiterrificRequester[ReqType] = {
    new GiterrificRequester(
      driver,
      baseUrl,
      repoName,
      refName,
      None
    )
  }
}

/**
 * Representation of a "full" requester for Giterrific server information. This requester is capable
 * of operating on the remote repository by retrieving commits, trees, and contents.
 *
 * @param driver The driver associated with the requester.
 * @param baseUrl The base URL associated with the requester.
 * @param repoName The relative path and name of the repository.
 * @param refName The name of the ref this requester interacts with.
 * @param path An optional path that the requester should operate at. Affects the results of tree and content retrieval.
 */
class GiterrificRequester[ReqType <: HttpReq[ReqType]](
  driver: HttpDriver[ReqType],
  baseUrl: String,
  repoName: String,
  refName: String,
  path: Option[String]
)(implicit ec: ExecutionContext) {
  implicit val formats = DefaultFormats

  private[this] val baseRequest: ReqType = driver.url(baseUrl) / "api" / "v1" / "repos" / repoName / "commits" / refName

  private[this] def prepareRequest(request: ReqType): ReqType = {
    request.withHeaders(Map(
      "Content-Type" -> "application/json",
      "Accept" -> "application/json"
    ))
  }

  private[this] def run[T](request: ReqType)(implicit mf: Manifest[T]): Future[T] = {
    driver.run(prepareRequest(request)).map { response =>
      parse(response).extract[T]
    }
  }

  private[this] def runRaw(request: ReqType): Future[InputStream] = {
    driver.runRaw(prepareRequest(request))
  }

  /**
   * Return a new instance of the requester without a path defined, which essentially positions this
   * requester at the root of the repository.
   */
  def withoutPath: GiterrificRequester[ReqType] = {
    new GiterrificRequester(
      driver,
      baseUrl,
      repoName,
      refName,
      None
    )
  }

  /**
   * Return a new instance of the requester defined at a particular path. This path isn't validated
   * until one of the server-facing get methods is invoked.
   *
   * @param path The new path to point the requester at. Could be a file or directory path.
   */
  def withPath(path: String): GiterrificRequester[ReqType] = {
    new GiterrificRequester(
      driver,
      baseUrl,
      repoName,
      refName,
      Some(path)
    )
  }

  /**
   * Retrieve a summary of recent commits on the ref defined by `refName` from the server.
   *
   * Since there may be quite a lot of commits in a repository, this method takes in two parameters:
   * `skip` and `maxCount` that should enable pagiantion.
   *
   * @param skip The number of commits to skip when generating the summary. Defaults to 0.
   * @param maxCount The number of commits to return when generating the summary. Defaults to 20.
   */
  def getCommits(skip: Int = 0, maxCount: Int = 20): Future[List[RepositoryCommitSummary]] = {
    run[List[RepositoryCommitSummary]](baseRequest.withQuery(Map(
      "skip" -> skip.toString,
      "maxCount" -> maxCount.toString
    )))
  }

  /**
   * Retrieve the tree of the repository, at a particular `path` if one is defined for this requester.
   */
  def getTree(): Future[List[RepositoryFileSummary]] = {
    val treeRequestBuilder = if (path.isEmpty) {
      baseRequest / "tree"
    } else {
      baseRequest / "tree" / path.getOrElse("")
    }

    run[List[RepositoryFileSummary]](treeRequestBuilder)
  }

  /**
   * Retrieve the contents of the file indicated by `path` for this requester. This method will fail
   * if no path is defined.
   */
  def getContents(): Future[RepositoryFileContent] = {
    if (path.isEmpty) {
      Future.failed(new IllegalStateException("You can only request content if a path is specified."))
    } else {
      val contentRequestBuilder = baseRequest / "contents" / path.getOrElse("")

      run[RepositoryFileContent](contentRequestBuilder)
    }
  }

  /**
   * Retrieve the raw contents of the file as an InputStream. Utilizing this method is required
   * for files larger than a certain size. Please note that the caller is responsible for closing
   * the InputStream.
   */
  def getRaw(): Future[InputStream] = {
    if (path.isEmpty) {
      Future.failed(new IllegalStateException("You can only request content if a path is specified."))
    } else {
      val contentRequestBuilder = baseRequest / "raw" / path.getOrElse("")

      runRaw(contentRequestBuilder)
    }
  }
}
