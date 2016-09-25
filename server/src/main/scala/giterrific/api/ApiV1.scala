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

import giterrific.core._
import giterrific.git._
import giterrific.git.JGitWrappers._
import giterrific.server.BuildInfo
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.LiftRules.DispatchPF
import net.liftweb.http.rest._
import net.liftweb.json._
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import org.eclipse.jgit.lib._

/**
 * The V1 API for Giterrific. Exposes all the repository information as JSON!
 */
object ApiV1 extends RestHelper with Loggable {
  val repoRoot = {
    Box.legacyNullTest(System.getProperty("giterrific.repos.root")).or(
      Props.get("giterrific.repos.root")
    ).openOr("repos")
  }
  val resolver = FileSystemRepositoryResolver(repoRoot)

  serve {
    case "api" :: "v1" :: "version" :: Nil Get _ =>
      Full(InMemoryResponse(
        BuildInfo.toJson.getBytes("utf8"),
        ("Content-Type" -> "application/json") :: Nil,
        Nil,
        200
      ))
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
              val commits = toCommitSummary(revwalk, commit, skip, maxCount)

              decompose(RepositoryCommitSummaryPage(
                ref = commitRef,
                totalCommitCount = countCommitsInTree(revwalk, commit),
                skip = skip,
                maxCount = maxCount,
                commits = commits.toList
              ))
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
                summary <- toFileSummary(treeWalk, filePath.length)
              } yield {
                decompose(summary)
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
                wholePath = req.path.wholePath.drop(3)
                parentDirectory = filePath.dropRight(1)
                fileName <- (wholePath.takeRight(1).headOption: Box[String])

                _ = navigateTreeToPath(treeWalk, parentDirectory)
                _ = filterTreeToFile(treeWalk, fileName)
                contents <- toFileContent(treeWalk) ~> 400
              } yield {
                decompose(contents)
              }
            }
          }

        case "commits" :: commitRef :: "raw" :: filePath Get req => repo =>
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
                wholePath = req.path.wholePath.drop(3)
                parentDirectory = filePath.dropRight(1)
                fileName <- wholePath.takeRight(1).headOption

                _ = navigateTreeToPath(treeWalk, parentDirectory)
                _ = filterTreeToFile(treeWalk, fileName)
                (size, inputStream) <- toFileStream(treeWalk)
              } yield {
                StreamingResponse(
                  inputStream,
                  ()=>logger.trace(s"Successfully streamed $size bytes of $fileName to client"),
                  size,
                  Nil,
                  Nil,
                  200
                )
              }
            }
          }
      }
    }
  }

  /**
   * Similar to Lift's DispatchPF, but the result type takes in a git repository and returns a
   * () => Box[LiftResponse].
   */
  type RepoDispatchPF = PartialFunction[Req, (Repository) => () => Box[LiftResponse]]

  /**
   * Execute a REST request with an identified repository.
   *
   * Thie method will automatically identify what part of the incoming request refers to the git
   * repository and then execute the partial function nested within it in the context of that
   * repository. The partial function passed in will need to accept the instantiated repository
   * as an argument.
   */
  private def withRepository(pf: RepoDispatchPF): DispatchPF = {
    new PartialFunction[Req, () => Box[LiftResponse]] {
      private def calculateGitRepoPath(req: Req) = {
        val gitRepoName = req.path.partPath.find(_.endsWith(".git")).getOrElse("")
        val gitRepoIndex = req.path.partPath.indexOf(gitRepoName)
        req.path.partPath.slice(0, gitRepoIndex+1)
      }

      def isDefinedAt(req: Req): Boolean = {
        val gitRepoPath = calculateGitRepoPath(req)

        resolver.exists(gitRepoPath.mkString("/")) &&
          pf.isDefinedAt(req.withNewPath(req.path.drop(gitRepoPath.length)))
      }

      def apply(req: Req): ()=>Box[LiftResponse] = {
        val gitRepoPath = calculateGitRepoPath(req)

        val result = resolver.withRespositoryFor(gitRepoPath.mkString("/")) { repo =>
          Full(pf.apply(req.withNewPath(req.path.drop(gitRepoPath.length))).apply(repo))
        }

        result match {
          case Full(innerResult) => innerResult
          case errorBox: EmptyBox => () => errorBox
        }
      }
    }
  }
}
