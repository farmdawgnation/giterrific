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
package giterrific

import giterrific.client._
import giterrific.driver.http._
import org.scalatest._
import play.api.libs.ws.ning._

class WS24ClientSpec extends ClientSpec[WS24HttpReq] with BeforeAndAfterAll {
  val wsClient = NingWSClient()
  val driver = WS24HttpDriver(wsClient)
  override val testClient = new GiterrificClient("http://localhost:8080", driver)

  override def afterAll(): Unit = {
    wsClient.close()
  }
}
