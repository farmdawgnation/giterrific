# Giterrific

_A lightweight JSON API for private git servers._

[![Build Status](https://travis-ci.org/farmdawgnation/giterrific.svg?branch=master)](https://travis-ci.org/farmdawgnation/giterrific)

Giterrific is a solution to a very silly problem: there doesn't exist (in as far as I could find)
a minimalist JSON API microservice that would surface information about bare git repositories on a
server in a sane fashion. So, I wrote Giterrific.

Giterrific surfaces basic information about your private Git repositories in simple, easy to
consume, JSON APIs that don't require running a full GitLab or GitHub Enterprise setup.

Using Giterrific with your private git server you can:

* Browse the commit history of repositories.
* Browse the tree of repositories.
* Retrieve JSON file content summaries from your repositories.
* Retrieve just the raw file from your repositories.

You can think of Giterrific a lot like Gitweb for your software. Where Gitweb presents information
about repositories in a way that humans will find useful to navigate, Giterrific presents information
about repositories in a way that software will find useful to navigate.

Giterrific's primary component is the **Giterrific Server** that actually exposes the information
about your projects.

## Giterrific Server

To take advantage of Giterrific you'll have to launch a Giterrific server.

The easiest way to use Giterrific Server is the Docker image. You can run it as follows:

```
docker run -p 8080:8080 -v /path/to/repos:/opt/giterrific/repos farmdawgnation/giterrific
```

This will boot a working Giterrific server that serves information about your git repositories.
After this is up and running you can start making calls against the endpoints that the server
exposes:

* `/api/v1/repos/:path/commits/:ref` - List the commits starting at a particular ref.
  * `:path` - Path to the repository relative to the repos root. Should end in `.git`
  * `:ref` - The ref (branch name, commit sha, etc) to list commits from.
* `/api/v1/repos/:path/commits/:ref/tree[/path/to/subfolders]` - List the contents of folders.
  * If no subfolder path is provided it lists the contents of the root of the repo's working
    directory.
* `/api/v1/repos/:path/commits/:ref/contents/[path/to/file]` - Retrieve the content summary of a file.
  * The path must reference exactly one file.
* `/api/v1/repos/:path/commits/:ref/raw/[path/to/file]` - Retrieve the raw file at the path.
  * For files of a certain size, this is the only way to retrieve their content.

## Giterrific Client for Scala

### Introduction

_Quick Links: [Latest API Docs](http://github.frmr.me/giterrific/api/latest/)_

Giterrific provides Scala language bindings from the giterrific-client project. This project uses
the same data model objects that we use on the server from giterrific-core and makes it trivial
to access information about your repositories in a structured manner.

The client is designed for use with pluggable HTTP implementations. We implement and provide a
default [Databinder Dispatch](https://github.com/dispatch/reboot) implementation, but make it
trivial to provide your own if you choose to do so.

### Pulling the client into your project

To use the client in your project, add the following to your `build.sbt` file.

```
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "me.frmr.giterrific" %% "giterrific-client" % "0.1.0-SNAPSHOT"

// Only if you want to use our default HTTP implementation
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
```

### Using the client

To use the client, you'll need to have a Giterrific Server set up somewhere. Once you've done that
you can start making requests against the repositories that server knows about. Below is some
example code that uses the test repo that is a part of this project.

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

### Custom HTTP drivers

If you wish to use some other HTTP implementation, you can also do that by implementing your own
version of the `HttpDriver` and `HttpReq` classes defined in HttpDriver.scala. An example of how
we did this for dispatch can be found in DispatchHttpDriver.scala.

After it's implemented, you can pass your new driver as the second argument to the GiterrificClient
constructor:

```scala
val client = new GiterrificClient("http://localhost:8080", myHttpDriver)
```

From now on methods that are based on this client will use your custom HTTP driver.

## About the Author

My name is Matt Farmer. By day I write code for [Domino Data Lab](https://dominodatalab.com).
By night, I contribute to various open source projects when I'm not binging something on Netflix.
