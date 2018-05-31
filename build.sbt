import play.core.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.{SbtAutoBuildPlugin, _}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "help-to-save-test-admin-frontend"

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()

val dependencies = Seq(
  ws,
  "uk.gov.hmrc" %% "govuk-template" % "5.20.0",
  "uk.gov.hmrc" %% "play-ui" % "7.14.0",
  "uk.gov.hmrc" %% "bootstrap-play-25" % "1.5.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1",
  "uk.gov.hmrc" %% "play-reactivemongo" % "5.2.0",
  "uk.gov.hmrc" %% "play-whitelist-filter" % "2.0.0",
  "com.github.kxbmap" %% "configs" % "0.4.4",
  "org.typelevel" %% "cats-core" % "1.1.0"
)

def testDependencies(scope: String = "test") = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
  "uk.gov.hmrc" %% "domain" % "5.1.0" % scope,
  "org.scalatest" %% "scalatest" % "3.0.5" % scope,
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope,
  "uk.gov.hmrc" %% "stub-data-generator" % "0.5.3" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope
)

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*(uk.gov.hmrc.helptosavetestadminfrontend.config|forms|util|views.*);.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum := 89,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val microservice = Project(appName, file("."))
  .settings(addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17"))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) ++ plugins: _*)
  .settings(playSettings ++ scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    libraryDependencies ++= appDependencies,
    //retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    //testGrouping in Test := oneForkedJvmPerTest((definedTests in Test).value),
    routesGenerator := StaticRoutesGenerator
  )
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo,
    "emueller-bintray" at "http://dl.bintray.com/emueller/maven" // for play json schema validator
  ))
