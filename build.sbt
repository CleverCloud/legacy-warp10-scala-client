lazy val http4sVersion = "0.15.3a"

lazy val root = (project in file(".")).
settings(
      inThisBuild(List(
            organization := "com.clevercloud",
            scalaVersion := "2.11.7",
            crossScalaVersions := Seq("2.11.7", "2.12.1"),
            version := "2.0.2",
            )),
      name := "warp10-scala-client",
      homepage := Some(url("https://github.com/CleverCloud/warp10-scala-client")),
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      libraryDependencies ++= Seq(
         "org.http4s" %% "http4s-blaze-client" % http4sVersion,
         "org.specs2" %% "specs2-core" % "3.8.8" % Test
         ),
      scalacOptions ++= Seq("-deprecation"),
      publishMavenStyle := true,
      Test / publishArtifact := false,
)
