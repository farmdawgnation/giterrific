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

import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}

case class WSHttpReq(
  url: String,
  headers: Map[String, String] = Map.empty,
  query: Map[String, String] = Map.empty
) extends HttpReq[WSHttpReq] {
  def withHeaders(headers: Map[String, String]): WSHttpReq =
    copy(headers = this.headers ++ headers)

  def withQuery(query: Map[String, String]): WSHttpReq =
    copy(query = this.query ++ query)

  def /(urlPart: String): WSHttpReq =
    copy(url = this.url + "/" + urlPart)
}

case class WSHttpDriver(wsClient: WSClient) extends HttpDriver[WSHttpReq] {
  def url(url: String): WSHttpReq = WSHttpReq(url)

  def run(request: WSHttpReq)(implicit ec: ExecutionContext): Future[String] = {
    val underlyingRequest = wsClient.url(request.url)
      .withHeaders(request.headers.toSeq: _*)
      .withQueryString(request.headers.toSeq: _*)

    underlyingRequest.get().map(_.body)
  }
}
