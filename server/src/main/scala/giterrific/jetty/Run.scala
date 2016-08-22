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
package giterrific.jetty

import java.io.File
import java.util.Date
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import net.liftweb.common.Box
import net.liftweb.util.Helpers.asInt

import sun.misc.Signal
import sun.misc.SignalHandler

object Run {
  private def logMessage(message: String) = {
    println(s"[BOOT ${new Date()}] $message")
  }

  def main(args: Array[String]): Unit = {
    logMessage("Welcome to Giterrific! Starting Jetty...")

    // Register the signal handler for USR2, which triggers a reload.
    Signal.handle(new Signal("USR2"), new SignalHandler {
      def handle(signal:Signal) {
        println("USR2")
      }
    })

    /* Calculate run.mode dependent path to logback configuration file.
    * Use same naming scheme as for props files.  */
    val logbackConfFile = {
     val propsDir = "props"
     val fileNameTail = "default.logback.xml"
     val mode = System.getProperty("run.mode")
     if (mode != null) propsDir + "/" + mode + "." + fileNameTail
     else propsDir + "/" + fileNameTail
    }
    /* set logback config file appropriately */
    logMessage(s"Setting logback configuration to $logbackConfFile...")
    System.setProperty("logback.configurationFile", logbackConfFile)

    /* choose different port for each of your webapps deployed on single server
     * you may use it in nginx proxy-pass directive, to target virtual hosts */
    val portNumber: Int = Box.legacyNullTest(System.getProperty("port")).flatMap(asInt).openOr(8080)
    logMessage(s"Configured port is $portNumber")

    val server = new Server(portNumber)
    val webapp = new WebAppContext

    webapp.setServer(server)
    webapp.setContextPath("/")

    /* use embeded webapp dir as source of the web content -> webapp
     * this is the dir within jar where we have put stuff with zip.
     * it was in a directory created by package, in target (also
     * named webapp), which was outside the jar. now, thanks to zip
     * it's inside so we need to use method below to get to it.
     * web.xml is in default location, of that embedded webapp dir,
     * so we don't have do webctx.setDescriptor */
    val webappDirInsideJar = webapp.getClass.getClassLoader.getResource("webapp").toExternalForm
    webapp.setWar(webappDirInsideJar)

    /* use resource base to avoid mixing your webapp files on the top level
     * of the executable jar, with all the included libraries etc
     * here I used webapp dir as it matches target dir of package-war task and makes
     * merging of webapp dir with output of assembly easier */
    //webapp.setResourceBase("webapp")
    /* also include webapp dir in path to web.xml */
    //webapp.setDescriptor(location.toExternalForm() + "/webapp/WEB-INF/web.xml")

    webapp.setServer(server)
    server.setHandler(webapp)

    logMessage("Booting up giterrific Lift application...")

    server.start
    server.join
  }
}
