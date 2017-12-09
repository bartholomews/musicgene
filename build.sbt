import play.sbt.PlayImport

name := """musicgene"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

// val reactiveMongoVer = "0.11.11"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,

  "it.turingtest" %% "spotify-scala-client" % "0.0.3",

  //===================================================================
                   // SPOTIFY API DEPENDENCIES todo delete //
  "com.google.protobuf" % "protobuf-java" % "2.5.0",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "net.sf.json-lib" % "json-lib" % "2.4" classifier "jdk15",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  //===================================================================
  // "org.projectlombok" % "lombok" % "1.16.6",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.specs2" %% "specs2-core" % "3.8.4" % "test",
  "org.mockito" % "mockito-all" % "1.9.5",
  "org.webjars" % "d3js" % "3.5.17",
  "org.webjars" % "c3" % "0.4.11",
  //"org.webjars" % "bootstrap" % "3.3.6", // exclude("org.webjars", "jquery"),
  // "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer,
  "org.mongodb" %% "casbah" % "3.0.0",
  "com.github.karelcemus" %% "play-redis" % "1.3.0-M1",
  PlayImport.cache exclude("net.sf.ehcache", "ehcache-core"),
  "org.seleniumhq.selenium" % "selenium-java" % "2.53.1"
)

/*
 https://www.playframework.com/documentation/2.4.0/ScalaRouting#Dependency-Injection
 http://reactivemongo.org/releases/0.11/documentation/tutorial/play2.html
 */
routesGenerator := InjectedRoutesGenerator

// resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
// resolvers += Resolver.mavenLocal

resolvers += Resolver.jcenterRepo
// resolvers += Resolver.bintrayRepo("bartholomews","maven")

scalacOptions in Test ++= Seq("-Yrangepos")

/*
add RequireJS plugin to the list of stages.
This allows the plugin to manipulate the JavaScript assets
when packaging the application as a jar.
DOESN'T PLAY WELL WITH HEROKU
*/
// pipelineStages := Seq(rjs)

// http://stackoverflow.com/a/22978218
sources in doc in Compile := List()
