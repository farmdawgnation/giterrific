//import sbtassembly.AssemblyKeys._

name := "giterrific-server"

version := GiterrificKeys.version

organization := "me.frmr.giterrific"

maintainer := "Matt Farmer <matt@frmr.me>"

packageSummary := "A lightweight JSON API for git repository servers."

packageDescription := "Giterrific surfaces information about your git repositories in a JSON API."

scalaVersion := GiterrificKeys.primaryScalaVersion

enablePlugins(JettyPlugin, JavaServerAppPackaging, JDebPackaging, DockerPlugin)

packageName in Docker := "farmdawgnation/giterrific"

defaultLinuxInstallLocation in Docker := "/opt/giterrific"

dockerBaseImage := "cantara/alpine-openjdk-jdk8"

dockerExposedPorts := Seq(8080)

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

lazy val addWebResources = taskKey[Int]("Add the web resources to the assembled jar")

addWebResources := Process(s"zip -r scala-2.11/${name.value}-${version.value}.jar webapp -x webapp/WEB-INF/lib*", new File("server/target")).!

lazy val assembledJar = taskKey[Int]("Build the fully assembled JAR including web resources")

val assembledJarTask = assembledJar := Def.sequential(
  (assembly in assembly).toTask(x => x),
  Keys.`package`,
  addWebResources
).value

// removes all jar mappings in universal and appends the fat jar
mappings in Universal := {
    // universalMappings: Seq[(File,String)]
    val universalMappings = (mappings in Universal).value
    val fatJar = (assembly in Compile).value
    Keys.`package`.value
    (addWebResources in Compile).value

    // removing means filtering
    val filtered = universalMappings filter {
        case (file, name) =>  ! name.endsWith(".jar")
    }

    // add the fat jar
    filtered :+ (fatJar -> ("lib/" + fatJar.getName))
}

// the bash scripts classpath only needs the fat jar
scriptClasspath := Seq( (jarName in assembly).value )
