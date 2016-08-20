package me.frmr.giterrific
package api

import giterrific.git._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.util._

object ApiV1 extends RestHelper {
  val prefixer = PrefixedIdentifier(4)
  val repoRoot = Props.get("giterrific.repos.root").openOr("")
  val resolver = FileSystemRepositoryResolver(repoRoot)

  serve {
    "api" / "v1" prefix {
      case "version" :: Nil JsonGet req =>
        ("name" -> "giterrific") ~
        ("version" -> "0.1.0")

      case "repos" :: id :: "commits" :: commitRef JsonGet req =>
        resolver.withRespositoryFor(prefixer.transform(id)) { repo =>
          Full(("action" -> "list-repo-commits"): JObject)
        }

      case "repos" :: id :: "commits" :: commitRef :: "tree" :: filePath JsonGet req =>
        resolver.withRespositoryFor(prefixer.transform(id)) { repo =>
          Full(("action" -> "list repo contents at commit and path"): JObject)
        }

      case "repos" :: id :: "commits" :: commitRef :: "contents" :: filePath JsonGet req =>
        resolver.withRespositoryFor(prefixer.transform(id)) { repo =>
          Full(("action" -> "display file contents at commit and path"): JObject)
        }
    }
  }
}
