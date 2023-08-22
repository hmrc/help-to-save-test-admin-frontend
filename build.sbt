val appName = "help-to-save-test-admin-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion := 2,
    scalaVersion := "2.13.8",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=routes;views"
    ),
    libraryDependencies += compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.13" cross CrossVersion.full)
    // ***************
  )
  .settings(
    PlayKeys.playDefaultPort := 7007
  )
  .settings(CodeCoverageSettings.settings *)
  .settings(scalafmtOnCompile := true)
