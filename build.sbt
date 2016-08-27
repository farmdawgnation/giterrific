import UnidocKeys._

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

lazy val playws25 =
  (project in file("extras/playws25"))
    .dependsOn(client)

lazy val playws24 =
  (project in file("extras/playws24"))
    .dependsOn(client)

lazy val giterrific =
  (project in file("."))
    .settings(unidocSettings: _*)
    .settings(
      name := "giterrific",
      unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(server),
      target in (ScalaUnidoc, unidoc) := baseDirectory.value / "docs" / "api" / GiterrificKeys.version,
      scalacOptions in (Compile, doc) ++= Opts.doc.title(s"Giterrific ${GiterrificKeys.version} API Documentation")
    )
    .aggregate(core, server, client)
