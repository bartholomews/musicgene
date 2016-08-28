name := """Genetic-Playlists"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  //===================================================================
                   // SPOTIFY API DEPENDENCIES //
  //"se.michaelthelin.spotify" % "spotify-web-api-java" % "1.5.0",
  //"com.google.guava" % "guava" % "18.0",
  "com.google.protobuf" % "protobuf-java" % "2.5.0",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "net.sf.json-lib" % "json-lib" % "2.4" classifier "jdk15",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  //===================================================================
  "org.projectlombok" % "lombok" % "1.16.6",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.webjars" % "d3js" % "3.5.17",
  "org.webjars" % "c3" % "0.4.11",
  // "org.webjars" % "jquery" % "2.2.4",
  "org.webjars" % "bootstrap" % "3.3.6", // exclude("org.webjars", "jquery"),
  // "org.webjars" % "bootstrap-switch" % "3.3.2"
  // "org.webjars" % "requirejs" % "2.2.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"


)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

/*
add RequireJS plugin to the list of stages.
This allows the plugin to manipulate the JavaScript assets
when packaging the application as a jar.
*/
// pipelineStages := Seq(rjs)

// https://www.playframework.com/documentation/2.4.0/ScalaRouting#Dependency-Injection
// http://reactivemongo.org/releases/0.11/documentation/tutorial/play2.html
routesGenerator := InjectedRoutesGenerator

// http://stackoverflow.com/a/22978218
sources in doc in Compile := List()
