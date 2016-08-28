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

package giterrific.driver.http

import java.util.concurrent.ExecutionException
import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}

case class WSHttpReq25(
  url: String,
  headers: Map[String, String] = Map.empty,
  query: Map[String, String] = Map.empty
) extends HttpReq[WSHttpReq25] {
  def withHeaders(headers: Map[String, String]): WSHttpReq25 =
    copy(headers = this.headers ++ headers)

  def withQuery(query: Map[String, String]): WSHttpReq25 =
    copy(query = this.query ++ query)

  def /(urlPart: String): WSHttpReq25 =
    copy(url = this.url + "/" + urlPart)
}

case class WSHttpDriver25(wsClient: WSClient) extends HttpDriver[WSHttpReq25] {
  def url(url: String): WSHttpReq25 = WSHttpReq25(url)

  def run(request: WSHttpReq25)(implicit ec: ExecutionContext): Future[String] = {
    val underlyingRequest = wsClient.url(request.url)
      .withHeaders(request.headers.toSeq: _*)
      .withQueryString(request.headers.toSeq: _*)

    underlyingRequest.get().flatMap { response =>
      if (response.status == 200) {
        Future.successful(response.body)
      } else {
        Future.failed(new ExecutionException(s"Upstream returned ${response.status} for request.", new RuntimeException(response.body)))
      }
    }
  }
}
