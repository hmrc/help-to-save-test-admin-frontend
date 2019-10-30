/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helptosavetestadminfrontend.config

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val runModeConfiguration: Configuration,
                          environment:              Environment,
                          servicesConfig:           ServicesConfig) {

  protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val assetsPrefix = loadConfig("assets.url") + loadConfig("assets.version")

  val runLocal: Boolean = servicesConfig.getBoolean("run-local")

  val clientId: String = servicesConfig.getString("microservice.services.oauth-frontend.client_id")
  val clientSecret: String = servicesConfig.getString("microservice.services.oauth-frontend.client_secret")

  val privilegedAccessClientId: String = servicesConfig.getString("privileged-access.client-id")
  val privilegedAccessTOTPSecret: String = servicesConfig.getString("privileged-access.totp-secret")

  val adminFrontendUrl: String = servicesConfig.getString("microservice.services.help-to-save-test-admin-frontend.url")

  val apiUrl: String = servicesConfig.getString("microservice.services.api.url")

  val oauthURL: String = servicesConfig.getString("microservice.services.oauth-frontend.url")
  val scopes = "read:help-to-save write:help-to-save"

  val authorizeCallbackForITests = s"$adminFrontendUrl/help-to-save-test-admin-frontend/authorize-callback-for-itests"
  def authorizeCallback(id: Option[UUID]): String =
    s"$adminFrontendUrl/help-to-save-test-admin-frontend/" +
    id.fold("authorize-callback-for-itests")(i ⇒ s"authorize-callback?id=${i.toString}")

  def authorizeUrl(id: UUID) = s"$oauthURL/oauth/authorize?client_id=$clientId&response_type=code&scope=$scopes&redirect_uri=${authorizeCallback(Some(id))}"

  val authLoginApiUrl: String = servicesConfig.getString("microservice.services.auth-login-api.url")
  val authUrl: String = servicesConfig.getString("microservice.services.auth.url")

}