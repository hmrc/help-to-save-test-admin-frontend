import play.core.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "help-to-save-test-admin-frontend"

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()

val akkaVersion     = "2.5.23"

val akkaHttpVersion = "10.0.15"


dependencyOverrides += "com.typesafe.akka" %% "akka-stream"    % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-protobuf"  % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-actor"     % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion

val dependencies = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-26" % "5.2.0",
  "uk.gov.hmrc" %% "govuk-template" % "5.66.0-play-26",
  "uk.gov.hmrc" %% "play-ui" % "9.2.0-play-26",
  "uk.gov.hmrc" %% "play-health" % "3.16.0-play-26",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "8.0.0-play-26",
  "uk.gov.hmrc" %% "play-whitelist-filter" % "3.4.0-play-26",
  "uk.gov.hmrc" %% "totp-generator" % "0.22.0",
  "com.github.kxbmap" %% "configs" % "0.6.1",
  "org.typelevel" %% "cats-core" % "2.2.0",
  "org.jsoup" % "jsoup" % "1.13.1",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.2.3",
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.3" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.3" % Provided cross CrossVersion.full
)

def testDependencies(scope: String = "test") = Seq(
  "uk.gov.hmrc" %% "bootstrap-test-play-26" % "5.2.0" % scope,
  "uk.gov.hmrc" %% "service-integration-test" % "1.1.0-play-26" % scope,
  "uk.gov.hmrc" %% "domain" % "5.11.0-play-26" % scope,
  "uk.gov.hmrc" %% "stub-data-generator" % "0.5.3" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "5.0.0-play-26" % scope,
  "org.scalatest" %% "scalatest" % "3.2.8" % scope,
  "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope,
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope
)

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*(uk.gov.hmrc.helptosavetestadminfrontend.config|forms|util|views.*);.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum := 10,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val microservice = Project(appName, file("."))
  .settings(addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17"))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins: _*)
  .settings(playSettings ++ scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(majorVersion := 2)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(scalaVersion := "2.12.13")
  .settings(PlayKeys.playDefaultPort := 7007)
  .settings(
    libraryDependencies ++= appDependencies,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo,
    "emueller-bintray" at "https://dl.bintray.com/emueller/maven" // for play json schema validator
  ))
  .settings(scalacOptions ++= Seq("-Xcheckinit","-feature","-deprecation"))
  .settings(scalacOptions += "-P:silencer:pathFilters=routes")
  .settings(scalacOptions += "-P:silencer:globalFilters=Unused import")
  .settings(Global / lintUnusedKeysOnLoad := false)

