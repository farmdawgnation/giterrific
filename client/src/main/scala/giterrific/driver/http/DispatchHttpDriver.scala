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

import com.ning.http.client.Response
import java.util.concurrent.ExecutionException
import java.io.InputStream
import scala.concurrent.{ExecutionContext, Future}

/**
 * A wrapper class for Dispatch's underlying Req instance.
 */
case class DispatchHttpReq(underlyingReq: dispatch.Req) extends HttpReq[DispatchHttpReq] {
  def withHeaders(headers: Map[String, String]): DispatchHttpReq =
    DispatchHttpReq(underlyingReq <:< headers)

  def withQuery(query: Map[String, String]): DispatchHttpReq =
    DispatchHttpReq(underlyingReq <<? query)

  def /(urlPart: String): DispatchHttpReq =
    DispatchHttpReq(underlyingReq / urlPart)
}

/**
 * An HTTP driver implementing Databinder Dispatch as the underlying HTTP library.
 */
case class DispatchHttpDriver() extends HttpDriver[DispatchHttpReq] {
  def url(url: String): DispatchHttpReq = DispatchHttpReq(dispatch.url(url))

  def run(request: DispatchHttpReq)(implicit ec: ExecutionContext): Future[String] = {
    dispatch.Http(request.underlyingReq OK dispatch.as.String)
  }

  def runRaw(request: DispatchHttpReq)(implicit ec: ExecutionContext): Future[InputStream] = {
    dispatch.Http(request.underlyingReq).flatMap { result =>
      if (result.getStatusCode() == 200) {
        Future.successful(result.getResponseBodyAsStream())
      } else {
        val statusCode = result.getStatusCode()
        val body = result.getResponseBody()

        Future.failed(new ExecutionException(
          new RuntimeException("Got status code $statusCode and response: $body")
        ))
      }
    }
  }
}
