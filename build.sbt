lazy val core =
  (project in file("core"))

lazy val server =
  (project in file("server"))
    .dependsOn(core)

lazy val client =
  (project in file("client"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .dependsOn(core)

lazy val giterrific =
  (project in file("."))
    .aggregate(core, server, client)
