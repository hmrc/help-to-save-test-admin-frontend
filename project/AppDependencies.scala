import play.core.PlayVersion
import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "7.11.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"   % hmrcBootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"           % "0.68.0",
    "uk.gov.hmrc"       %% "play-allowlist-filter"        % "1.0.0-play-28",
    "uk.gov.hmrc"       %% "totp-generator"               % "0.22.0",
    "com.github.kxbmap" %% "configs"                      % "0.6.1",
    "org.typelevel"     %% "cats-core"                    % "2.2.0",
    "org.jsoup"         %  "jsoup"                        % "1.13.1",
    "org.mongodb.scala" %% "mongo-scala-driver"           % "4.2.3",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "5.0.0-play-28",
  )

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "org.scalatestplus"       %% "mockito-3-12"                 % "3.2.10.0"          % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"           % "5.1.0"             % scope,
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"       % hmrcBootstrapVersion    % scope,
    "uk.gov.hmrc"             %% "domain"                       % "6.2.0-play-28"     % scope,
    "uk.gov.hmrc"             %% "stub-data-generator"          % "0.5.3"             % scope,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"      % "0.68.0"            % scope,
    "org.scalatest"           %% "scalatest"                    % "3.2.9"             % scope,
    "org.scalatestplus"       %% "scalatestplus-scalacheck"     % "3.1.0.0-RC2"       % scope,
    "com.vladsch.flexmark"    % "flexmark-all"                  % "0.62.2"            % scope,
    "com.typesafe.play"       %% "play-test"                    % PlayVersion.current % scope,
    "org.scalamock"           %% "scalamock-scalatest-support"  % "3.6.0"             % scope
  )
}
