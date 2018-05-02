organization := """com.clevercloud"""

name := """warp10-scala-client"""

version := "2.0.2-SNAPSHOT"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.7", "2.12.4")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "org.specs2" %% "specs2-core" % "3.8.8" % Test
)

bintrayOrganization := Some("clevercloud")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Xcheckinit",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard",
  "-language:postfixOps"
)

parallelExecution in Test := false

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
