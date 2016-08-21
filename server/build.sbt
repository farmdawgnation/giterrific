name := "giterrific-server"

version := GiterrificKeys.version

organization := "me.frmr.giterrific"

scalaVersion := GiterrificKeys.primaryScalaVersion

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
    "org.eclipse.jetty" % "jetty-webapp" % "9.2.1.v20140609",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.1.7",
    "org.eclipse.jgit"  % "org.eclipse.jgit"    % "4.4.1.201607150455-r"
  )
}

scalacOptions in Test ++= Seq("-Yrangepos")
