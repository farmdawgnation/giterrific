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

The easiest way to use Giterrific Server is the Docker image. You can run the latest stable as
follows:

```
docker run -p 8080:8080 -v /path/to/repos:/opt/giterrific/repos farmdawgnation/giterrific
```

If you're interested, instead, in booting the latest snapshot just add `:snapshot` to the end of
that command to pull the snapshot tag.

This will boot a working Giterrific server that serves information about your git repositories.

Since 0.2.0, Giterrific's REST API is documented with [Swagger](https://swagger.io). You can find
a copy of Swagger UI pointed at our Swagger file [on the github pages site](http://github.frmr.me/giterrific/swagger/).
If you have a copy of 0.2.0 or higher running on Docker locally, you can even use the Swagger UI
to test the API calls yourself directly from your browser.

### Versioning

Giterrific follows a modified [Semantic Versioning](http://semver.org). Our version numbers are
denotes using three numbers, separated by dots in the form of `MAJOR.MINOR.PATCH`. However, in
addition to the version of the server we also have different versions of the API. For everyone's
sanity, we've decided to use the `MAJOR` version of the Giterrific server to indicate the latest
stable version of the API.

So, for example:

* Changes to the `PATCH` version may introduce backward-compatible bug fixes to any of the APIs that
  Giterrific exposes.
* Changes to the `MINOR` version may introduce backward-compatible changes to the stable API, but
  breaking changes to the experimental API.
* Changes to the `MAJOR` version denote the current experimental API becoming stable.

So, if Giterrific's version is `0.1.0` then the APIs under `/api/v1` are considered experimental
and may change in a breaking manner between minor releases. If Giterrific's version is `1.0.0` then
the APIs under `/api/v1` are considered stable and the APIs under `/api/v2` are considered
experimental.

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

To use the client in your project, add the following to your `build.sbt` file:

```scala
libraryDependencies += "me.frmr.giterrific" %% "giterrific-client" % "0.2.0"

// Only if you want to use our default HTTP implementation
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
```

If you're feeling dangerous, you can also experiment with the latest snapshot by adding the
following to your `build.sbt`:

```scala
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "me.frmr.giterrific" %% "giterrific-client" % "0.3.0-SNAPSHOT"

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
