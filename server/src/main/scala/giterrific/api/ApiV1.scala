package giterrific
package api

import giterrific.git._
import giterrific.git.JGitWrappers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json._
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.util._
import net.liftweb.util.Helpers._

object ApiV1 extends RestHelper {
  val transformer = ChainedTransformer(Seq(PrefixedIdentifier(4), DotGitSuffixer))
  val repoRoot = Props.get("giterrific.repos.root").openOr("")
  val resolver = FileSystemRepositoryResolver(repoRoot)

  serve {
    "api" / "v1" prefix {
      case "version" :: Nil JsonGet req =>
        ("name" -> "giterrific") ~
        ("version" -> "0.1.0")

      case "repos" :: id :: "commits" :: commitRef :: Nil JsonGet req =>
        println(transformer.transform(id))
        resolver.withRespositoryFor(transformer.transform(id)) { repo =>
          withRevWalkFor(repo) { revwalk =>
            val skip: Int = S.param("skip").flatMap(asInt).openOr(0)
            val maxCount: Int = S.param("maxCount").flatMap(asInt).openOr(20)

            for {
              ref <- getRef(repo, commitRef)
              commit <- getCommit(revwalk, ref)
            } yield {
              decompose(toCommitSummary(revwalk, commit, skip, maxCount))
            }
          }
        }

      case "repos" :: id :: "commits" :: commitRef :: "tree" :: filePath JsonGet req =>
        resolver.withRespositoryFor(transformer.transform(id)) { repo =>
          Full(("action" -> "list repo contents at commit and path"): JObject)
        }

      case "repos" :: id :: "commits" :: commitRef :: "contents" :: filePath JsonGet req =>
        resolver.withRespositoryFor(transformer.transform(id)) { repo =>
          Full(("action" -> "display file contents at commit and path"): JObject)
        }
    }
  }
}
