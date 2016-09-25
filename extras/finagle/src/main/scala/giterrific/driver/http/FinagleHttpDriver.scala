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

import com.twitter.finagle._
import java.io.InputStream
import java.net.URL
import java.util.concurrent.ExecutionException
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * A data structure representing a request that will be used with Finagle's HTTP library.
 *
 * @param url The URL to issue the request to.
 * @param headers The headers to use for the request.
 * @param query The query string parameters for the request.
 */
case class FinagleHttpReq(
  url: String,
  headers: Map[String, String] = Map.empty,
  query: Map[String, String] = Map.empty
) extends HttpReq[FinagleHttpReq] {
  def withHeaders(headers: Map[String, String]): FinagleHttpReq =
    copy(headers = this.headers ++ headers)

  def withQuery(query: Map[String, String]): FinagleHttpReq =
    copy(query = this.query ++ query)

  def /(urlPart: String): FinagleHttpReq =
    copy(url = this.url + "/" + urlPart)

  private[http] lazy val completeUrl: String = {
    if (query == Map.empty) {
      url
    } else {
      url + "?" + query.toList.map {
        case (key, value) =>
          "$key=$value"
      }.mkString("&")
    }
  }
}

/**
 * An implementation of Giterrific's HttpDriver abstraction driven by Finagle.
 */
case class FinagleHttpDriver() extends HttpDriver[FinagleHttpReq] {
  def url(url: String) = FinagleHttpReq(url)

  def run(request: FinagleHttpReq)(implicit ec: ExecutionContext): Future[String] = {
    issueRequest(request).flatMap { httpResponse =>
      if (httpResponse.statusCode == 200) {
        Future.successful(httpResponse.getContentString())
      } else {
        Future.failed(new ExecutionException(
          new RuntimeException(
            s"Server returned ${httpResponse.statusCode} with response: ${httpResponse.getContentString}"
          )
        ))
      }
    }
  }

  def runRaw(request: FinagleHttpReq)(implicit ec: ExecutionContext): Future[InputStream] = {
    issueRequest(request).flatMap { httpResponse =>
      if (httpResponse.statusCode == 200) {
        Future.successful(httpResponse.getInputStream())
      } else {
        Future.failed(new ExecutionException(
          new RuntimeException(
            s"Server returned ${httpResponse.statusCode} with response: ${httpResponse.getContentString}"
          )
        ))
      }
    }
  }

  private[this] def issueRequest(request: FinagleHttpReq)(implicit ec: ExecutionContext): Future[http.Response] = {
    val twitterRequest = constructTwitterRequest(request)
    val (hostname, port) = computeHostAndPort(request)
    val client = Http.newService(s"$hostname:$port")
    toScalaFuture(client(twitterRequest))
  }

  // Converts our request format into an actual request object we can pass into Finagle.
  private[this] def constructTwitterRequest(giterrificRequest: FinagleHttpReq): http.Request = {
    val builder = http.RequestBuilder().url(giterrificRequest.completeUrl)

    val builderWithHeaders = giterrificRequest.headers.toList.foldLeft(builder) { (currentBuilder, header) =>
      currentBuilder.setHeader(header._1, header._2)
    }

    builderWithHeaders.buildGet()
  }

  private[this] def computeHostAndPort(giterrificRequest: FinagleHttpReq): (String, Int) = {
    val url = new URL(giterrificRequest.completeUrl)

    val hostname = url.getHost()
    val port = if (url.getPort() == -1) {
      url.getDefaultPort()
    } else {
      url.getPort()
    }

    (hostname, port)
  }

  private[this] def toScalaFuture[T](twitterFuture: com.twitter.util.Future[T])(implicit mf: Manifest[T]): Future[T] = {
    val resultPromise = Promise[T]()
    twitterFuture.respond(twitterTry => resultPromise.complete(twitterTry.asScala))
    resultPromise.future
  }
}
