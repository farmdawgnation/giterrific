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

/**
 * A client that is used to talk to a Giterrific server.
 *
 * The client accepts a `baseUrl` and a `driver`. The driver is the shim through which Giterrific
 * interacts with the underlying HTTP interface.
 *
 * == Drivers ==
 *
 * If you aren't opinionated about what driver
 * you'd like you use, you can use the provided [[giterrific.driver.http.DispatchHttpDriver DispatchHttpDriver]]
 * which is backed by Databinder Dispatch. To use that, you'll need to make sure that you indlude
 * Dispatch's core in your dependencies in your `build.sbt` file like so:
 *
 * {{{
 * libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
 * }}}
 *
 * After you've included Dispatch and Giterrific in your project, instantiating a client should be
 * pretty straightforward:
 *
 * {{{
 * import giterrific.client._
 * val client = new GiterrificClient("http://giterrifichostname:8080")
 * }}}
 *
 * === Alternate drivers ===
 *
 * If you prefer to use some other HTTP implementation you have a few options. Giterrific provides
 * drivers backed by Play's WS library for Play 2.4 and Play 2.5. For Play 2.4 you'll need to pull
 * in the `giterrific-playws24` library:
 *
 * {{{
 * libraryDependencies += "me.frmr.giterrific.extras" %% "giterrific-playws24" % "0.1.0"
 * }}}
 *
 * Likewise, if you use Play 2.5, you should pull in the `giterrific-playws25` library.
 *
 * {{{
 * libraryDependencies += "me.frmr.giterrific.extras" %% "giterrific-playws25" % "0.1.0"
 * }}}
 *
 * These libraries implement [[giterrific.driver.http.WS24HttpDriver WS24HttpDriver]] and
 * and [[giterrific.driver.http.WS25HttpDriver WS25HttpDriver]] respectively.
 *
 * == Using the client ==
 *
 * The client is designed to be used in a builder pattern. One created a client is meant to be used
 * to access a number of different repositories and refs on your server. To start, you'll need to
 * select what repository you'd like to access. You do this by invoking `repo` with the relative
 * path to the repository on the server. The repository must end in ".git" for the server to
 * recognize it correctly.
 *
 * {{{
 * val projectsRepo = client.repo("mfarmer/testprojects.git")
 * }}}
 *
 * From there you select a ref:
 *
 * {{{
 * projectRepo.withRef("master")
 * }}}
 *
 * And from there you can start invoking different operations available on
 * [[giterrific.client.GiterrificRequester GiterrificRequester]].
 *
 * @param baseUrl The base URL of your Giterrific server.
 * @param driver The HTTP driver that this client should use under the hood.
 */
class GiterrificClient[ReqType <: HttpReq[ReqType]](
  baseUrl: String,
  driver: HttpDriver[ReqType] = DispatchHttpDriver()
)(implicit ec: ExecutionContext) {
  /**
   * Selects a repositoy on the server to interact with.
   *
   * The name of the repository is the same as its relative path, relative to the repo root for
   * the giterrific server. The validity of the repository isn't validated at this point.
   *
   * @param repoName The name (relative path to) of the repository you'd like to interact with.
   */
  def repo(repoName: String): GiterrificRequesterBase[ReqType] = {
    new GiterrificRequesterBase(
      driver,
      baseUrl,
      repoName
    )
  }
}
