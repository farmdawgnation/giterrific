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
  val baseRequestBuilder = url(baseUrl) / "repos" / repoName / "commits" / refName

  def withPath(path: String): GiterrificRequester = {
    new GiterrificRequester(
      baseUrl,
      repoName,
      refName,
      Some(path)
    )
  }

  def getCommits(skip: Int = 0, maxCount: Int = 20): Future[RepositoryCommitSummary] = {
    Http(baseRequestBuilder OK as.String).map { response =>
      parse(response).extract[RepositoryCommitSummary]
    }
  }

  def getTree(): Future[RepositoryFileSummary] = {
    val treeRequestBuilder = if (path.isEmpty) {
      baseRequestBuilder / "tree"
    } else {
      baseRequestBuilder / "tree" / path.mkString("/")
    }

    Http(treeRequestBuilder OK as.String).map { response =>
      parse(response).extract[RepositoryFileSummary]
    }
  }

  def getContent(): Future[RepositoryFileContent] = {
    if (path.isEmpty) {
      Future.failed(new IllegalStateException("You can only request content if a path is specified."))
    } else {
      val contentRequestBuilder = baseRequestBuilder / "content" / path.mkString("/")

      Http(contentRequestBuilder OK as.String).map { response =>
        parse(response).extract[RepositoryFileContent]
      }
    }
  }
}
