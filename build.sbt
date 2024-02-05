val appName = "help-to-save-test-admin-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion := 2,
    scalaVersion := "2.13.11",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test()
  )
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")
  .settings(scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s")
  .settings(PlayKeys.playDefaultPort := 7007)
  .settings(CodeCoverageSettings.settings *)
  .settings(scalafmtOnCompile := true)

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
