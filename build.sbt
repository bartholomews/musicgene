name := """Genetic-Playlists"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  //"se.michaelthelin.spotify" % "spotify-web-api-java" % "1.5.0",
  //"com.google.guava" % "guava" % "18.0",
  "com.google.protobuf" % "protobuf-java" % "2.5.0",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "net.sf.json-lib" % "json-lib" % "2.4" classifier "jdk15",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "org.projectlombok" % "lombok" % "1.16.6",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.webjars" % "d3js" % "3.5.17"

)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
