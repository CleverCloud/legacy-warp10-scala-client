name := """warp10-scala-client"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.4" % "test"

lazy val http4sVersion = "0.14.1a"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-client" % http4sVersion
)


// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"
