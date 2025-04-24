import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "9.11.0"
  val playVersion = "play-30"
  val hmrc = "uk.gov.hmrc"

  val compile: Seq[ModuleID] = Seq(
    hmrc            %% s"bootstrap-frontend-$playVersion" % hmrcBootstrapVersion,
    (hmrc           %% "totp-generator"                   % "0.26.0").cross(CrossVersion.for3Use2_13),
    "org.typelevel" %% "cats-core"                        % "2.13.0",
    hmrc            %% s"play-frontend-hmrc-$playVersion" % "11.13.0"
  )

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    hmrc %% s"bootstrap-test-$playVersion" % hmrcBootstrapVersion % scope
  )
}
