package giterrific

import giterrific.client._
import giterrific.driver.http._
import org.scalatest._
import org.scalatestplus.play.guice._
import play.api.libs.ws.ahc._

class WS25ClientSpec extends ClientSpec[WS25HttpReq] with GuiceFakeApplicationFactory with BeforeAndAfterAll {
  val app = fakeApplication()
  implicit lazy val materializer = app.materializer
  val wsClient = AhcWSClient()
  val driver = WS25HttpDriver(wsClient)
  override val testClient = new GiterrificClient("http://localhost:8080", driver)

  override def afterAll(): Unit = {
    wsClient.close()
    app.stop()
  }
}
