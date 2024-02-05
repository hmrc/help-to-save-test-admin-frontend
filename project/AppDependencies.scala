import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "7.15.0"
  val hmrcMongoVersion = "0.68.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % hmrcBootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "totp-generator"             % "0.24.0",
    "org.typelevel"     %% "cats-core"                  % "2.9.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "7.29.0-play-28"
  )

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-28"  % hmrcBootstrapVersion % scope,
    "org.mockito"       %% "mockito-scala"           % "1.17.12"            % scope,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % hmrcMongoVersion     % scope
  )
}
