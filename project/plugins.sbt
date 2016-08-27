// PGP plugin
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

//Enable the sbt web plugin
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.1.0")

// Assembly plugin
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

// Unidoc plugin
addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.3")

// for autoplugins
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))
