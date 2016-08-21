package giterrific.client

import scala.concurrent.ExecutionContext

class GiterrificClient(
  baseUrl: String
)(implicit ec: ExecutionContext) {
  def repo(repoName: String): GiterrificRequesterBase = {
    new GiterrificRequesterBase(
      baseUrl,
      repoName
    )
  }
}
