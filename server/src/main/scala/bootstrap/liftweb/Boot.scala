package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import net.liftmodules.JQueryModule
import net.liftweb.http.js.jquery._

import giterrific.api.ApiV1

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("giterrific")

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index" // the simple way to declare a menu
    )

    LiftRules.setSiteMap(SiteMap(entries:_*))

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Add the api
    LiftRules.statelessDispatch.append(ApiV1)

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery1113
    JQueryModule.init()

    LiftRules.securityRules = () => {
      SecurityRules(content = Some(ContentSecurityPolicy(
        scriptSources = List(
            ContentSourceRestriction.Self),
        styleSources = List(
            ContentSourceRestriction.Self)
            )))
    }
  }
}
