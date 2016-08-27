package giterrific

import org.scalatest._

class ClientSpec extends FeatureSpec with GivenWhenThen {
  info("As a consumer of the giterrific client")
  info("I want my client to be query the details of my repository")
  info("So that I can use that information in my own code")

  feature("Retrieving commit information") {
    scenario("Refers to an invalid repository") {
      pending
    }

    scenario("Refers to an invalid ref") {
      pending
    }

    scenario("Refers to a valid ref") {
      pending
    }
  }

  feature("Retrieving tree information") {
    scenario("Refers to an invalid repository") {
      pending
    }

    scenario("Refers to an invalid ref") {
      pending
    }

    scenario("Refers to a valid ref at the root") {
      pending
    }

    scenario("Refers to a valid ref at a valid subpath") {
      pending
    }

    scenario("Refers to a valid ref at an invalid subpath") {
      pending
    }
  }

  feature("Retrieving file content") {
    scenario("Refers to an invalid repository") {
      pending
    }

    scenario("Refers to an invalid ref") {
      pending
    }

    scenario("Refers to a valid ref without a file specified") {
      pending
    }

    scenario("Refers to a valid ref with a valid file specified") {
      pending
    }

    scenario("Refers to a valid ref with an invalid file specified") {
      pending
    }
  }
}
