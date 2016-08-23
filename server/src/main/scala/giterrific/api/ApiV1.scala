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
import org.eclipse.jgit.lib._

object ApiV1 extends RestHelper with Loggable {
  val repoRoot = {
    Box.legacyNullTest(System.getProperty("giterrific.repos.root")).or(
      Props.get("giterrific.repos.root")
    ).openOr("repos")
  }
  val resolver = FileSystemRepositoryResolver(repoRoot)

  def withRepository(pf: PartialFunction[Req, (Repository) => () => Box[LiftResponse]]): PartialFunction[Req, () => Box[LiftResponse]] = {
    new PartialFunction[Req, () => Box[LiftResponse]] {
      def isDefinedAt(req: Req): Boolean = {
        val gitRepoName = req.path.partPath.find(_.endsWith(".git")).getOrElse("")
        val gitRepoIndex = req.path.partPath.indexOf(gitRepoName)
        val gitRepoPath = req.path.partPath.slice(0, gitRepoIndex+1)
        resolver.exists(gitRepoPath.mkString("/")) && pf.isDefinedAt(req.withNewPath(req.path.drop(gitRepoPath.length)))
      }

      def apply(req: Req): ()=>Box[LiftResponse] = {
        val gitRepoName = req.path.partPath.find(_.endsWith(".git")).getOrElse("")
        val gitRepoIndex = req.path.partPath.indexOf(gitRepoName)
        val gitRepoPath = req.path.partPath.slice(0, gitRepoIndex+1)

        val result = resolver.withRespositoryFor(gitRepoPath.mkString("/")) { repo =>
          Full(pf.apply(req.withNewPath(req.path.drop(gitRepoPath.length))).apply(repo))
        }

        result match {
          case Full(innerResult) =>
            innerResult
          case o: EmptyBox =>
            () => o
        }
      }
    }
  }

  serve {
    "api" / "v1" / "repos" prefix {
      withRepository {
        case "commits" :: commitRef :: Nil JsonGet req => repo =>
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

        case "commits" :: commitRef :: "tree" :: filePath JsonGet req => repo =>
          withRevWalkFor(repo) { revWalk =>
            withTreeWalkFor(repo) { treeWalk =>
              for {
                ref <- getRef(repo, commitRef)
                commit <- getCommit(revWalk, ref)
                commitTree = getCommitTree(commit)
                _ <- addTree(treeWalk, commitTree)
                _ = navigateTreeToPath(treeWalk, filePath)
              } yield {
                decompose(toFileSummary(treeWalk, filePath.length))
              }
            }
          }

        case "commits" :: commitRef :: "contents" :: filePath JsonGet req => repo =>
          withRevWalkFor(repo) { revWalk =>
            withTreeWalkFor(repo) { treeWalk =>
              for {
                ref <- getRef(repo, commitRef)
                commit <- getCommit(revWalk, ref)
                commitTree = getCommitTree(commit)
                _ <- addTree(treeWalk, commitTree)

                // Lift's RestHelper typically uses the suffix of a file name to let the client
                // indicate what type of response it would like to receive. The "filePath" above
                // won't have the file extension on the last item as a result. So we retrieve the
                // entire path from the request itself and drop the prefix.
                wholePath = req.path.wholePath.drop(5)
                parentDirectory = filePath.dropRight(1)
                fileName <- wholePath.takeRight(1).headOption

                _ = navigateTreeToPath(treeWalk, parentDirectory)
                _ = filterTreeToFile(treeWalk, fileName)
                contents <- toFileContent(treeWalk)
              } yield {
                decompose(contents)
              }
            }
          }
      }
    }
  }
}
