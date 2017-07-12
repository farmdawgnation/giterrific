name := "giterrific-client"

organization := "me.frmr.giterrific"

version := GiterrificKeys.version

scalaVersion := GiterrificKeys.primaryScalaVersion

scalacOptions ++= GiterrificKeys.defaultScalacOptions

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.13.1" % "provided,it"

libraryDependencies += "net.liftweb" %% "lift-json" % "3.1.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "it"

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("Sonatype Snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("Sonatype Releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sonatype")

pomExtra :=
<url>https://github.com/farmdawgnation/giterrific</url>
<licenses>
  <license>
    <name>Apache 2</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    <distribution>repo</distribution>
  </license>
</licenses>
<scm>
  <url>https://github.com/farmdawgnation/giterrific.git</url>
  <connection>https://github.com/farmdawgnation/giterrific.git</connection>
</scm>
<developers>
  <developer>
    <id>farmdawgnation</id>
    <name>Matt Farmer</name>
    <email>matt@frmr.me</email>
  </developer>
</developers>
