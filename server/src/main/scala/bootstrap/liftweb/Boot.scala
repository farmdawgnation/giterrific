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
package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import json._
import json.JsonDSL._
import net.liftmodules.JQueryModule
import net.liftweb.http.js.jquery._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import giterrific.api.ApiV1

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {
    // Booting!
    logger.info(s"Booting Giterrific in ${Props.mode} mode.")

    // where to search snippet
    LiftRules.addToPackages("giterrific")

    // Build SiteMap
    val entries = List(Menu.i("Home") / "index")
    LiftRules.setSiteMap(SiteMap(entries:_*))

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Add the api
    LiftRules.statelessDispatch.append(ApiV1)

    // Custom 404 behavior
    LiftRules.uriNotFound.prepend {
      case (req, _) if req.acceptsJson_? && ! req.acceptsStarStar =>
        NotFoundAsResponse(
          JsonResponse(("error" -> "The resource you were looking for could not be found."): JObject, Nil, Nil, 404)
        )

      case (req, _) =>
        NotFoundAsResponse(
          PlainTextResponse("The resource you were looking for could not be found.\n", Nil, 404)
        )
    }

    // Custom 500 behavior
    LiftRules.responseTransformers.prepend {
      case response if response.toResponse.code >= 500 =>
        JsonResponse(
          ("error" -> "An internal server error was experienced. Please check the Giterrific log."): JObject,
          Nil,
          Nil,
          response.toResponse.code
        )

      case response => response
    }

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery1113
    JQueryModule.init()

    LiftRules.securityRules = () => {
      SecurityRules(
        content = Some(ContentSecurityPolicy(
          scriptSources = List(ContentSourceRestriction.Self),
          styleSources = List(ContentSourceRestriction.Self)
        ))
      )
    }
  }
}
