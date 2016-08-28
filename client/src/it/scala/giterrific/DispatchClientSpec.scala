package giterrific

import giterrific.client._

import giterrific.driver.http._

class DispatchClientSpec extends ClientSpec[DispatchHttpReq] {
  override val testClient = new GiterrificClient("http://localhost:8080")
}
