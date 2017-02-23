organization := """com.clevercloud"""

name := """warp10-scala-client"""

version := "2.0.1"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.7", "2.12.1")

libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.8" % Test

lazy val http4sVersion = "0.15.3a"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-client" % http4sVersion
)

bintrayOrganization := Some("clevercloud")

scalacOptions ++= Seq("-deprecation")

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
