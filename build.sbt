name := """musicgene"""
organization := "io.bartholomews"
homepage := Some(url("https://github.com/bartholomews/musicgene"))

version := "0.0.1-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, homepage, scalaVersion, sbtVersion),
    buildInfoPackage := "musicgene"
  )

scalaVersion := "2.13.2"

routesImport ++= Seq(
  "io.bartholomews.spotify4s.entities._",
  "model.music.GeneratedPlaylistResultId"
)

libraryDependencies += guice
libraryDependencies ++= Seq(
  "io.bartholomews" %% "spotify4s" % "0.0.0+65-d112bde0+20200607-1030-SNAPSHOT",
  "io.bartholomews" %% "discogs4s" % "0.0.1+5-c8522079+20200525-0002-SNAPSHOT",
  // https://github.com/pauldijou/jwt-scala/releases
  "com.pauldijou" %% "jwt-play" % "4.2.0",
  // https://mvnrepository.com/artifact/com.adrianhurt/play-bootstrap
  "com.adrianhurt" %% "play-bootstrap" % "1.6.1-P28-B4"
)

// https://github.com/playframework/scalatestplus-play/releases
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "io.bartholomews.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "io.bartholomews.binders._"
