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

import scala.concurrent.{ExecutionContext, Future}

/**
 * A trait representing a generic HTTP request.
 *
 * If you choose to implement a custom driver for Giterrific, you should also implement its custom
 * HttpReq.
 */
trait HttpReq[UnderlyingType <: HttpReq[UnderlyingType]] {
  def withHeaders(headers: Map[String, String]): UnderlyingType
  def withQuery(query: Map[String, String]): UnderlyingType
  def /(urlPart: String): UnderlyingType
}

/**
 * A trait representing a generic HTTP driver that Giterrific can use to talk to the Giterrific
 * server. Implement this in your project and provide it to the Giterrific client at construction
 * if you'd like to use something other than our standard Dispatch implementation.
 */
trait HttpDriver[ReqType <: HttpReq[_]] {
  def run(request: ReqType)(implicit ec: ExecutionContext): Future[String]
  def url(url: String): ReqType
}
