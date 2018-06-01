/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {
  override protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val assetsPrefix = loadConfig("assets.url") + loadConfig("assets.version")

  val htsTestAdminUrl: String = getString("microservice.services.help-to-save-test-admin-frontend.url")

   val clientId = getString("microservice.services.oauth-frontend.client_id")
   val clientSecret = getString("microservice.services.oauth-frontend.client_secret")

  def oAuthRedirectUrl(htsUrl: String): String =
    s"/oauth/authorize?client_id=$clientId&response_type=code&scope=read:help-to-save&redirect_uri=$htsUrl"

  val eligibilityAuthorizeCallback = s"$htsTestAdminUrl/help-to-save-test-admin-frontend/eligibility-authorize-callback"
}