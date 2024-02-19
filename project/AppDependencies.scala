import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "7.15.0"
  val hmrcMongoVersion = "0.68.0"
  val playVersion = "play-28"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion" % hmrcBootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "totp-generator"             % "0.24.0",
    "org.typelevel"     %% "cats-core"                  % "2.9.0",
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion"         % "8.5.0"
  )

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion"  % hmrcBootstrapVersion % scope,
    "org.mockito"       %% "mockito-scala"           % "1.17.12"            % scope,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion     % scope
  )
}
