import play.core.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "help-to-save-test-admin-frontend"

lazy val appDependencies: Seq[ModuleID] = dependencies ++ testDependencies()

val dependencies = Seq(
  ws,
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"   % "5.12.0",
  "uk.gov.hmrc"       %% "govuk-template"               % "5.72.0-play-28",
  "uk.gov.hmrc"       %% "play-ui"                      % "9.5.0-play-28",
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"           % "0.68.0",
  "uk.gov.hmrc"       %% "play-allowlist-filter"        % "1.0.0-play-28",
  "uk.gov.hmrc"       %% "totp-generator"               % "0.22.0",
  "com.github.kxbmap" %% "configs"                      % "0.6.1",
  "org.typelevel"     %% "cats-core"                    % "2.2.0",
  "org.jsoup"         %  "jsoup"                        % "1.13.1",
  "org.mongodb.scala" %% "mongo-scala-driver"           % "4.2.3",
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.5" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.5" % Provided cross CrossVersion.full
)

def testDependencies(scope: String = "test") = Seq(
  "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % scope,
"org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
  "uk.gov.hmrc"           %% "bootstrap-test-play-28"      % "5.12.0"            % scope,
  "uk.gov.hmrc"           %% "service-integration-test"    % "1.1.0-play-28"     % scope,
  "uk.gov.hmrc"           %% "domain"                      % "6.2.0-play-28"     % scope,
  "uk.gov.hmrc"           %% "stub-data-generator"         % "0.5.3"             % scope,
  "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-28"    % "0.68.0"             % scope,
  "org.scalatest"         %% "scalatest"                   % "3.2.9"             % scope,
  "org.scalatestplus"     %% "scalatestplus-scalacheck"    % "3.1.0.0-RC2"       % scope,
  "com.vladsch.flexmark"   % "flexmark-all"        % "0.62.2"            % scope,
  "com.typesafe.play"     %% "play-test"                   % PlayVersion.current % scope,
  "org.scalamock"         %% "scalamock-scalatest-support" % "3.6.0"             % scope
)

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*(uk.gov.hmrc.helptosavetestadminfrontend.config|forms|util|views.*);.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimumStmtTotal := 10,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
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
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo,
    "emueller-bintray" at "https://dl.bintray.com/emueller/maven" // for play json schema validator
  ))
  .settings(scalacOptions ++= Seq("-Xcheckinit","-feature","-deprecation"))
  .settings(scalacOptions += "-P:silencer:pathFilters=routes")
  .settings(scalacOptions += "-P:silencer:globalFilters=Unused import")
  .settings(Global / lintUnusedKeysOnLoad := false)

