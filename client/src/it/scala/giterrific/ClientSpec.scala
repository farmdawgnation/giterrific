package giterrific

import giterrific.core._
import giterrific.client._
import giterrific.driver.http._
import org.scalatest._
import org.scalatest.RecoverMethods._
import net.liftweb.json._
import net.liftweb.json.Extraction._
import scala.io.Source

trait ClientSpec[ReqType <: HttpReq[ReqType]] extends AsyncFeatureSpec with GivenWhenThen {
  implicit val formats = DefaultFormats
  val testClient: GiterrificClient[ReqType]

  info("As a consumer of the giterrific client")
  info("I want my client to be query the details of my repository")
  info("So that I can use that information in my own code")

  feature("Retrieving commit information") {
    scenario("Refers to an invalid repository") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("foobar").withRef("master").getCommits()
      }
    }

    scenario("Refers to an invalid ref") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("test.git").withRef("foobar").getCommits()
      }
    }

    scenario("Refers to a valid ref") {
      val expectedJson = """
      |{
      |  "ref": "master",
      |  "skip": 0,
      |  "maxCount": 20,
      |  "totalCommitCount": 1,
      |  "commits": [
      |    {
      |      "sha":"558faad4023f2690e35c8fd234666408223a4301",
      |      "author":{
      |        "date":"2016-08-20T19:44:35Z",
      |        "name":"Matt Farmer",
      |        "email":"matt@frmr.me"
      |      },
      |      "committer":{
      |        "date":"2016-08-20T19:44:35Z",
      |        "name":"Matt Farmer",
      |        "email":"matt@frmr.me"
      |      },
      |      "message":"Initial checking\n"
      |    }
      |  ]
      |}
      |""".stripMargin

      testClient.repo("test.git").withRef("master").getCommits().map { result =>
        assert(parse(expectedJson).extract[RepositoryCommitSummaryPage] == result)
      }
    }
  }

  feature("Retrieving tree information") {
    scenario("Refers to an invalid repository") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("foobar").withRef("master").getTree()
      }
    }

    scenario("Refers to an invalid ref") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("test.git").withRef("foobar").getTree()
      }
    }

    scenario("Refers to a valid ref at the root") {
      val expectedJson = """
      |[
      |  {
      |    "name":"files",
      |    "path":"files",
      |    "mode":"40000",
      |    "isDirectory":true,
      |    "size":33
      |  },
      |  {
      |    "name":"hello.txt",
      |    "path":"hello.txt",
      |    "mode":"100644",
      |    "isDirectory":false,
      |    "size":12
      |  }
      |]
      |""".stripMargin

      testClient.repo("test.git").withRef("master").getTree().map { result =>
        assert(parse(expectedJson).extract[List[RepositoryFileSummary]] == result)
      }
    }

    scenario("Refers to a valid ref at a valid subpath") {
      val expectedJson = """
      |[
      |  {
      |    "name":".keep",
      |    "path":"files/.keep",
      |    "mode":"100644",
      |    "isDirectory":false,
      |    "size":0
      |  }
      |]
      """.stripMargin

      testClient.repo("test.git").withRef("master").withPath("files").getTree().map { result =>
        assert(parse(expectedJson).extract[List[RepositoryFileSummary]] == result)
      }
    }

    scenario("Refers to a valid ref at an invalid subpath") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("test.git").withRef("master").withPath("fileszzz").getTree()
      }
    }
  }

  feature("Retrieving file content") {
    scenario("Refers to an invalid repository") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("foobar").withRef("master").withPath("hello.txt").getContents()
      }
    }

    scenario("Refers to an invalid ref") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("test.git").withRef("foobar").withPath("hello.txt").getContents()
      }
    }

    scenario("Refers to a valid ref without a file specified") {
      recoverToSucceededIf[java.lang.IllegalStateException] {
        testClient.repo("test.git").withRef("foobar").getContents()
      }
    }

    scenario("Refers to a valid ref with a valid file specified") {
      val expectedJson = """
      |{
      |  "name":"hello.txt",
      |  "content":"aGVsbG8gbW9tXG4K",
      |  "encoding":"base64",
      |  "size":12
      |}
      |""".stripMargin

      testClient.repo("test.git").withRef("master").withPath("hello.txt").getContents().map { result =>
        assert(parse(expectedJson).extract[RepositoryFileContent] == result)
      }
    }

    scenario("Refers to a valid ref with an invalid file specified") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("test.git").withRef("master").withPath("foobar.txt").getContents()
      }
    }
  }

  feature("Retrieving raw files") {
    scenario("Refers to an invalid repository") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("foobar").withRef("master").withPath("hello.txt").getRaw()
      }
    }

    scenario("Refers to an invalid ref") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("test.git").withRef("foobar").withPath("hello.txt").getRaw()
      }
    }

    scenario("Refers to a valid ref without a file specified") {
      recoverToSucceededIf[java.lang.IllegalStateException] {
        testClient.repo("test.git").withRef("foobar").getRaw()
      }
    }

    scenario("Refers to a valid ref with a valid file specified") {
      testClient.repo("test.git").withRef("master").withPath("hello.txt").getRaw().map { result =>
        val resultString = Source.fromInputStream(result).mkString
        result.close()

        assert("hello mom\\n\n" == resultString)
      }
    }

    scenario("Refers to a valid ref with an invalid file specified") {
      recoverToSucceededIf[java.util.concurrent.ExecutionException] {
        testClient.repo("test.git").withRef("master").withPath("foobar.txt").getRaw()
      }
    }
  }
}
