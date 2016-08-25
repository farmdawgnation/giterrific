# Giterrific

_A lightweight JSON API for private git servers._

Giterrific is a solution to a very silly problem: there doesn't exists (in as far as I could find)
a minimalist JSON API microservice that would surface information about git repositories in a sane
fashion. So, I wrote Giterrific.

Giterrific surfaces basic information about your private Git repositories in simple, easy to
consume, JSON APIs that don't require running a full GitLab or GitHub Enterprise setup. It's still
very much in alpha, so plan accordingly.

## Giterrific Server

The easiest way to use Giterrific Server for now is the Docker image. You can run it as follows:

```
docker run -p 8080:8080 -v /path/to/repos:/opt/docker/repos farmdawgnation/giterrific
```

This will boot a working Giterrific server that serves information about your git repositories.
After this is up and running you can start making calls against the endpoints that the server
exposes:

* `/api/v1/repos/:id/commits/:ref` - List the commits starting at a particular ref.
  * `:id` - The repository identifier.
  * `:ref` - The ref (branch name, commit sha, etc) to list commits from.
* `/api/v1/repos/:id/commits/:ref/tree[/path/to/subfolders]` - List the contents of folders.
  * If no subfolder path is provided it lists the contents of the root of the repo's working
    directory.
* `/api/v1/repos/:id/commits/:ref/contents/[path/to/file]` - Retrieve the content summary of a file.
  * The path must reference exactly one file.

## Giterrific Scala Bindings

If you use Scala and want to use Giterrific from your Scala app, we've provided language bindings
specifically for you!

To use them with their default, [Dispatch](https://github.com/dispatch/reboot) based HTTP
implementation, add the following to your build.sbt file:

```
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "me.frmr.giterrific" %% "giterrific-client" % "0.1.0-SNAPSHOT"

// Only if you want to use our default HTTP implementation
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
```

After you've done the above, you can instantiate a new instance of the GiterrificClient and start
interacting with the Giterrific server.

```scala
// Import the client bindings
import giterrific.client._

// Set your execution context
implicit val ec = scala.concurrent.ExecutionContext.global

// Declare a client
val client = new GiterrificClient("http://localhost:8080")

// Pick a repo
val testRepo = client.repo("testRepo")

// List commits on master
testRepo.withRef("master").getCommits()
testRepo.withRef("master").getCommits(skip = 0, maxCount = 20)

// List the root folder
testRepo.withRef("master").getTree()
// List the files folder
testRepo.withRef("master").withPath("files").getTree()

// Get the contents of hello.txt at the root
tesRepo.withRef("master").withPath("hello.txt").getContent()
```

If you wish to use some other HTTP implementation, you can also do that by implementing your own
version of the `HttpDriver` and `HttpReq` classes defined in HttpDriver.scala. An example of how
we did this for dispatch can be found in DispatchHttpDriver.scala.

After it's implemented, you can pass your new driver as the second argument to the GiterrificClient
constructor

```scala
val client = new GiterrificClient("http://localhost:8080", myHttpDriver)
```

And Giterrific will use your HTTP implementation of choice.
