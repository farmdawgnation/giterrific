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

import java.io.InputStream
import java.util.concurrent.ExecutionException
import play.api.libs.ws._
import play.api.libs.ws.ning._
import scala.concurrent.{ExecutionContext, Future}

/**
 * A data structure representing a request that will be used with Play 2.4's implementation of the
 * WS library.
 *
 * @param url The URL to issue the request to.
 * @param headers The headers to use for the request.
 * @param query The query string parameters for the request.
 */
case class WS24HttpReq(
  url: String,
  headers: Map[String, String] = Map.empty,
  query: Map[String, String] = Map.empty
) extends HttpReq[WS24HttpReq] {
  def withHeaders(headers: Map[String, String]): WS24HttpReq =
    copy(headers = this.headers ++ headers)

  def withQuery(query: Map[String, String]): WS24HttpReq =
    copy(query = this.query ++ query)

  def /(urlPart: String): WS24HttpReq =
    copy(url = this.url + "/" + urlPart)
}

/**
 * An implementation of a Giterrific HTTP driver based on Play 2.4's implementation of the WS library.
 *
 * @param wsClient The WS Client that should be used to make requests.
 */
case class WS24HttpDriver(wsClient: WSClient) extends HttpDriver[WS24HttpReq] {
  def url(url: String): WS24HttpReq = WS24HttpReq(url)

  def run(request: WS24HttpReq)(implicit ec: ExecutionContext): Future[String] = {
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

  def runRaw(request: WS24HttpReq)(implicit ec: ExecutionContext): Future[InputStream] = {
    val underlyingRequest = wsClient.url(request.url)
      .withHeaders(request.headers.toSeq: _*)
      .withQueryString(request.headers.toSeq: _*)

    underlyingRequest.get().flatMap { response =>
      if (response.status == 200) {
        response match {
          case ningResponse: NingWSResponse =>
            Future.successful(ningResponse.ahcResponse.getResponseBodyAsStream())

          case unknownResponse =>
            Future.failed(new ExecutionException(
              new RuntimeException("Got an unknown type of WSResponse. Needed NingWSResponse.")
            ))
        }
      } else {
        Future.failed(new ExecutionException(s"Upstream returned ${response.status} for request.", new RuntimeException(response.body)))
      }
    }
  }
}
