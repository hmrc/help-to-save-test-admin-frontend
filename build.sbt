val appName = "help-to-save-test-admin-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion := 2,
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s"
  )
  .settings(
    PlayKeys.playDefaultPort := 7007
  )
  .settings(CodeCoverageSettings.settings: _*)
