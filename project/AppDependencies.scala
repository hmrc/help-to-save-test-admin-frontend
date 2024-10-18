import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "9.0.0"
  val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion" % hmrcBootstrapVersion,
    "uk.gov.hmrc"       %% "totp-generator"                   % "0.25.0",
    "org.typelevel"     %% "cats-core"                        % "2.10.0",
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion" % "8.5.0"
  )

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion"  % hmrcBootstrapVersion % scope,
    "org.mockito"       %% "mockito-scala"                 % "1.17.30"            % scope,
  )
}
