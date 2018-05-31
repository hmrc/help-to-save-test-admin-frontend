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

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector
import uk.gov.hmrc.helptosavetestadminfrontend.forms.NinoForm
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.helptosavetestadminfrontend.views
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

class HelpToSaveApiController @Inject()(http: WSHttp, authConnector: AuthConnector)(implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  var tokenCache: LoadingCache[String, String] = _

  def loadCache(implicit hc: HeaderCarrier, request: Request[_]): LoadingCache[String, String] = {
    if (tokenCache == null) {
      tokenCache =
        CacheBuilder
          .newBuilder
          .maximumSize(1)
          .expireAfterWrite(3, TimeUnit.HOURS)
          .build(new CacheLoader[String, String] {
            override def load(key: String): String = {
              val result = Await.result(authConnector.loginAndGetToken(), Duration(1, TimeUnit.MINUTES))
              result match {
                case Right(token) =>
                  logger.info(s"Loaded access token from oauth, token=$token")
                  token
                case Left(e) => throw new Exception(s"error during retrieving token from oauth, error=$e")
              }
            }
          })
    }

    tokenCache
  }

  def availableEndpoints(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.availableEndpoints()))
  }

  def getCheckEligibilityPage(): Action[AnyContent] = Action.async { implicit request =>
    Try {
      loadCache
      tokenCache.get("token")
    } match {
      case Success(token) =>
        logger.info(s"token exists in cache, token: $token")
        Future.successful(Ok(views.html.get_check_eligibility_page(NinoForm.ninoForm)))
      case Failure(e) =>
        logger.warn(e.getMessage)
        Future.successful(internalServerError())
    }
  }

  def checkEligibility(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("inside checkEligibility"))
  }

  def authorizeCallback(code: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"inside authorizeCallback, code=$code")
    http.post(s"${appConfig.oauthURL}/oauth/token", Json.parse(appConfig.tokenRequest(code)))
      .map {
        response =>
          response.status match {
            case OK =>
              val accessToken = (response.json \ "access_token").as[String]
              Ok(accessToken)
            case other: Int =>
              logger.warn(s"got $other status during get access_token, body=${response.body}")
              internalServerError()
          }
      }.recover {
      case ex ⇒
        logger.warn(s"error during /oauth/token, error=${ex.getMessage}")
        internalServerError()
    }
  }
}
