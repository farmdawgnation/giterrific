//import sbtassembly.AssemblyKeys._

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
    "ch.qos.logback"    % "logback-classic"     % "1.1.7",
    "org.eclipse.jgit"  % "org.eclipse.jgit"    % "4.4.1.201607150455-r"
  )
}

scalacOptions in Test ++= Seq("-Yrangepos")

test in assembly := {}

assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

mainClass in assembly := Some("giterrific.jetty.Run")

assemblyMergeStrategy in assembly := {
  case "pom.properties" => MergeStrategy.discard
  case "pom.xml" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
