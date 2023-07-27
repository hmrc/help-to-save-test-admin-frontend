import play.core.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName = "help-to-save-test-admin-frontend"

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val microservice = Project(appName, file("."))
  .settings(addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17"))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins: _*)
  .settings(CodeCoverageSettings.settings *)
  .settings(scalaSettings: _*)
  .settings(majorVersion := 2)
  .settings(defaultSettings(): _*)
  .settings(scalaVersion := "2.12.16")
  .settings(PlayKeys.playDefaultPort := 7007)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo,
    "emueller-bintray" at "https://dl.bintray.com/emueller/maven" // for play json schema validator
  ))
  .settings(scalacOptions ++= Seq("-Xcheckinit", "-feature", "-deprecation"))
  .settings(scalacOptions += "-P:silencer:pathFilters=routes")
  .settings(scalacOptions += "-P:silencer:globalFilters=Unused import")
  .settings(Global / lintUnusedKeysOnLoad := false)
  .settings(Test / parallelExecution := false)
