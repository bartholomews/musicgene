name := """musicgene"""
organization := "io.bartholomews"
homepage := Some(url("https://github.com/bartholomews/musicgene"))

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, homepage, scalaVersion, sbtVersion),
    buildInfoPackage := "musicgene"
  )

scalaVersion := "2.13.1"

routesImport += "io.bartholomews.spotify4s.entities._"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "io.bartholomews" %% "spotify4s" % "0.1.0-SNAPSHOT",
  "io.bartholomews" %% "discogs4s" % "0.1.0-SNAPSHOT",
  "com.pauldijou" %% "jwt-play" % "4.2.0",
  // https://mvnrepository.com/artifact/com.adrianhurt/play-bootstrap
  "com.adrianhurt" %% "play-bootstrap" % "1.6.1-P28-B4"
)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "io.bartholomews.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "io.bartholomews.binders._"
