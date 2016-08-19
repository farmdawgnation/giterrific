name := "giterrific"

version := "0.1.0"

organization := "me.frmr"

scalaVersion := "2.11.7"

enablePlugins(JettyPlugin)

resolvers ++= Seq("snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "https://oss.sonatype.org/content/repositories/releases")

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "3.0-RC3"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftmodules"   %% "lift-jquery-module_3.0" % "2.9",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.1.7",
    "org.eclipse.jgit"  % "org.eclipse.jgit"    % "4.4.1.201607150455-r"
  )
}

scalacOptions in Test ++= Seq("-Yrangepos")
