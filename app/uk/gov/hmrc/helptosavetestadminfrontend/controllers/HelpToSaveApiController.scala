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

import java.util.UUID

import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging

import scala.concurrent.Future

class HelpToSaveApiController @Inject()(http: WSHttp)(implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  var accessToken = ""

  def authLoginStubCallback: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(SeeOther(appConfig.oAuthRedirectUrl(appConfig.eligibilityAuthorizeCallback)))
  }

  def eligibilityAuthorizeCallback: Action[AnyContent] = Action.async { implicit request =>
    http.post(s"${appConfig.oauthURL}/oauth/token", body(request.queryString.get("code")))
      .map {

        response =>

          response.status match {
            case OK | CREATED =>
              accessToken = (response.json \ "access_token").as[String]
              Ok("saved access_token")
            case other: Int =>
              logger.warn(s"got $other status during get access_token, body=${response.body}")
              InternalServerError
          }
      }
  }

  def handleOauthTokenCallback(): Action[AnyContent] = Action.async { implicit request =>
   Future.successful(Ok("success"))
  }

  def body(maybeCode: Option[Seq[String]]): String = {
    val code = maybeCode.getOrElse(Seq("")).head
    s"client_secret=${appConfig.clientSecret}&client_id=${appConfig.clientId}&grant_type=authorization_code&redirect_uri=${appConfig.oauthTokenCallback}&code=$code"
  }

  def checkEligibility(nino: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info("inside checkEligibility")
    val headers = Map("Content-Type" -> "application/json",
      "Accept" -> "application/vnd.hmrc.2.0+json",
      "Gov-Client-User-ID" -> "EL069651A",
      "Gov-Client-Timezone" -> "UTC",
      "Gov-Vendor-Version" -> "1.3",
      "Gov-Vendor-Instance-ID" -> UUID.randomUUID().toString,
      "Authorization" -> s"Bearer $accessToken",
      "Cache-Control" -> "no-cache",
      "Postman-Token" -> UUID.randomUUID().toString
    )

    http.get(s"${appConfig.apiHost}/individuals/help-to-save/eligibility/$nino", headers)
      .map {
        response =>
          response.status match {
            case OK => Ok(response.body)
            case other: Int =>
              logger.warn(s"got $other status during get eligibility_check, body=${response.body}")
              InternalServerError
          }
      }
  }
}
