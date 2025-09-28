ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "24.2.0",
  "com.twitter" %% "finagle-http" % "24.2.0",
  "org.playframework" %% "play-json" % "3.0.5",
  "com.typesafe" % "config" % "1.4.5",
  "com.google.inject" % "guice" % "7.0.0",
  "ch.qos.logback" % "logback-classic" % "1.5.18",
  "org.slf4j" % "slf4j-api" % "2.0.17",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.mockito" %% "mockito-scala" % "2.0.0" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "ImdbService"
  )
