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

package uk.gov.hmrc.helptosavetestadminfrontend.controllers

import java.util.concurrent.TimeUnit

import com.google.common.cache.{Cache, CacheBuilder}
import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector
import uk.gov.hmrc.helptosavetestadminfrontend.forms.NinoForm
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.helptosavetestadminfrontend.views

import scala.concurrent.Future

class HelpToSaveApiController @Inject()(http: WSHttp, authConnector: AuthConnector)(implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  val tokenCache: Cache[String, String] =
    CacheBuilder
      .newBuilder
      .maximumSize(1)
      .expireAfterWrite(3, TimeUnit.HOURS)
      .build()

  def availableEndpoints(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(SeeOther(routes.HelpToSaveApiController.availableEndpoints().url))
  }

  def getCheckEligibilityPage(): Action[AnyContent] = Action.async { implicit request =>

    val maybeToken = Option(tokenCache.getIfPresent("token"))

    maybeToken match {
      case Some(token) => logger.info("token exists in cache, not going through login processl")
      case None =>
        logger.info("no access token in the cache, getting the token and redirecting to getCheckEligibilityPage")
        val p = authConnector.loginAndGetToken()
    }

    Future.successful(Ok(views.html.get_check_eligibility_page(NinoForm.ninoForm)))
  }


  def checkEligibility(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("inside checkEligibility"))
  }

  def authorizeCallback(code: String): Action[AnyContent] = Action.async { implicit request =>
    http.post(s"${appConfig.oauthURL}/oauth/token", Json.parse(appConfig.tokenRequest(code)), Map("Content-Type" -> "application/json"))
      .map {
        response =>
          response.status match {
            case OK =>

              val accessToken = (response.json \ "access_token").as[String]

              tokenCache.put("token", accessToken)

              logger.info(s"updated token cache with token: $accessToken")

              SeeOther(accessToken)

            case other: Int =>
              logger.warn(s"got $other status during get access_token, body=${response.body}")
              InternalServerError
          }
      }
  }
}
