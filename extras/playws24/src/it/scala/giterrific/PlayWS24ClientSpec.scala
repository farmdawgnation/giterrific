package giterrific

import giterrific.client._
import giterrific.driver.http._
import org.scalatest._
import play.api.libs.ws.ning._

class PlayWS24ClientSpec extends ClientSpec[WSHttpReq] with BeforeAndAfterAll {
  val wsClient = NingWSClient()
  val driver = WSHttpDriver(wsClient)
  override val testClient = new GiterrificClient("http://localhost:8080", driver)

  override def afterAll(): Unit = {
    wsClient.close()
  }
}
