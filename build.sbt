
lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    routesImport ++= Seq(
      "views.spotify.responses._",
      "io.bartholomews.musicgene.controllers._",
      "io.bartholomews.spotify4s.entities._"
    ),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "io.bartholomews" %% "spotify4s" % "0.0.0+57-1741a301-SNAPSHOT",
      "io.bartholomews" %% "discogs4s" % "0.0.1+13-bbb90ce2-SNAPSHOT",
      // https://github.com/pauldijou/jwt-scala/releases
      "com.pauldijou" %% "jwt-play" % "4.2.0",
      // https://mvnrepository.com/artifact/com.adrianhurt/play-bootstrap
      "com.adrianhurt" %% "play-bootstrap" % "1.6.1-P28-B4",
      "com.vmunier" %% "scalajs-scripts" % "1.1.4",
      guice,
      specs2 % Test
    )
  )
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, homepage, scalaVersion, sbtVersion),
    buildInfoPackage := "io.bartholomews.musicgene"
  )
  .dependsOn(sharedJvm)

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.0.0"
    )
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.13.1",
  organization := "io.bartholomews"
)
