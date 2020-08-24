import play.core.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.{SbtArtifactory, SbtAutoBuildPlugin, _}
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
  "uk.gov.hmrc" %% "bootstrap-frontend-play-26" % "2.24.0",
  "uk.gov.hmrc" %% "govuk-template" % "5.56.0-play-26",
  "uk.gov.hmrc" %% "play-ui" % "8.8.0-play-26",
  "uk.gov.hmrc" %% "play-health" % "3.14.0-play-26",
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.30.0-play-26",
  "uk.gov.hmrc" %% "play-whitelist-filter" % "3.1.0-play-26",
  "uk.gov.hmrc" %% "totp-generator" % "0.21.0",
  "com.github.kxbmap" %% "configs" % "0.4.4",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.jsoup" % "jsoup" % "1.13.1",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0"
)

def testDependencies(scope: String = "test") = Seq(
  "uk.gov.hmrc" %% "bootstrap-play-26" % "1.3.0" % scope classifier "tests",
  "uk.gov.hmrc" %% "service-integration-test" % "0.12.0-play-26" % scope,
  "uk.gov.hmrc" %% "domain" % "5.9.0-play-26" % scope,
  "uk.gov.hmrc" %% "stub-data-generator" % "0.5.3" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.21.0-play-26" % scope,
  "org.scalatest" %% "scalatest" % "3.0.8" % scope,
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
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins: _*)
  .settings(playSettings ++ scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(majorVersion := 2)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(scalaVersion := "2.12.11")
  .settings(PlayKeys.playDefaultPort := 7007)
  .settings(
    libraryDependencies ++= appDependencies,
    //retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    //testGrouping in Test := oneForkedJvmPerTest((definedTests in Test).value),
  )
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo,
    "emueller-bintray" at "http://dl.bintray.com/emueller/maven" // for play json schema validator
  ))
  .settings(scalacOptions ++= Seq("-Xcheckinit","-feature","-deprecation"))
