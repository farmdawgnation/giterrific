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

import scala.concurrent.ExecutionContext
import giterrific.driver.http._

class GiterrificClient[ReqType <: HttpReq[ReqType]](
  baseUrl: String,
  driver: HttpDriver[ReqType] = DispatchHttpDriver()
)(implicit ec: ExecutionContext) {
  def repo(repoName: String): GiterrificRequesterBase[ReqType] = {
    new GiterrificRequesterBase(
      driver,
      baseUrl,
      repoName
    )
  }
}
