package me.frmr.giterrific
package api

import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object ApiV1 extends RestHelper {
  serve {
    "api" / "v1" prefix {
      case "version" :: Nil JsonGet req =>
        ("name" -> "giterrific") ~
        ("version" -> "0.1.0")

      case "repos" :: Nil JsonGet req =>
        ("action" -> "list-repos"): JObject

      case "repos" :: id :: "commits" :: commitRef JsonGet req =>
        ("action" -> "list-repo-commits"): JObject

      case "repos" :: id :: "commits" :: commitRef :: "tree" :: filePath JsonGet req =>
        ("action" -> "list repo contents at commit and path"): JObject

      case "repos" :: id :: "commits" :: commitRef :: "contents" :: filePath JsonGet req =>
        ("action" -> "display file contents at commit and path"): JObject
    }
  }
}
