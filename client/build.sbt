name := "giterrific-client"

organization := "me.frmr.giterrific"

version := GiterrificKeys.version

scalaVersion := GiterrificKeys.primaryScalaVersion

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"

libraryDependencies += "net.liftweb" %% "lift-json" % "3.0-RC3"
