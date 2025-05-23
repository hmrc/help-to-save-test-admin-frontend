val appName = "help-to-save-test-admin-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion := 2,
    scalaVersion := "3.3.5",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test()
  )
  .settings(scalafmtOnCompile := true)
  .settings(scalacOptions += "-Wconf:src=routes/.*:s,src=html/.*:s")
  .settings(PlayKeys.playDefaultPort := 7007)
  .settings(CodeCoverageSettings.settings *)
  // Disable default sbt Test options (might change with new versions of bootstrap)
  .settings(Test / testOptions -= Tests
    .Argument("-o", "-u", "target/test-reports", "-h", "target/test-reports/html-report"))
  // Suppress successful events in Scalatest in standard output (-o)
  // Options described here: https://www.scalatest.org/user_guide/using_scalatest_with_sbt
  .settings(
    Test / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u",
      "target/test-reports",
      "-h",
      "target/test-reports/html-report"))
