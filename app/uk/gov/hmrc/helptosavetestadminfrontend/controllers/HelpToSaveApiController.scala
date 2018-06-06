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
import java.util.concurrent.TimeUnit

import com.google.common.cache._
import com.google.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector
import uk.gov.hmrc.helptosavetestadminfrontend.forms.EligibilityRequestForm
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.helptosavetestadminfrontend.views

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class HelpToSaveApiController @Inject()(http: WSHttp, authConnector: AuthConnector)(implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  val tokenCache: LoadingCache[String, String] =
    CacheBuilder
      .newBuilder
      .maximumSize(1)
      .expireAfterWrite(3, TimeUnit.MINUTES)
      .removalListener(new RemovalListener[String, String] {
        override def onRemoval(notification: RemovalNotification[String, String]): Unit = {
          logger.info(s"cache entry: (${notification.getKey}) has been removed due to ${notification.getCause}")
        }
      })
      .build(new CacheLoader[String, String] {
        override def load(key: String): String = {
          UUID.randomUUID().toString
        }
      })

  def availableEndpoints(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.availableEndpoints()))
  }

  def getCheckEligibilityPage(): Action[AnyContent] = Action.async { implicit request =>
    Try {
      tokenCache.get("token")
    } match {
      case Success(token) =>
        logger.info(s"loaded token from cache, token: $token")
        Future.successful(Ok(views.html.get_check_eligibility_page(EligibilityRequestForm.eligibilityForm)))
      case Failure(e) =>
        logger.warn(e.getMessage)
        Future.successful(internalServerError())
    }
  }

  def checkEligibility(): Action[AnyContent] = Action.async { implicit request =>
    EligibilityRequestForm.eligibilityForm.bindFromRequest().fold(
      formWithErrors ⇒ Future.successful(Ok(views.html.get_check_eligibility_page(formWithErrors))),
      {
        params =>

          val headers =
            Map("Content-Type" -> params.contentType,
              "Accept" -> params.accept,
              "Gov-Client-User-ID" -> params.govClientUserId,
              "Gov-Client-Timezone" -> params.govClientTimezone,
              "Gov-Vendor-Version" -> params.govVendorVersion,
              "Gov-Vendor-Instance-ID" -> params.govVendorInstanceId,
              "Authorization" -> s"Bearer ${tokenCache.get("token")}",
              "Cache-Control" -> params.cacheControl
            )

          http.get(s"${appConfig.apiUrl}/individuals/help-to-save/eligibility/${params.nino}", headers)
            .map {
              response =>
                response.status match {
                  case OK => logger.info(s"eligibility response body= ${response.body}")
                  case other: Int =>
                    logger.warn(s"got $other status during get eligibility_check, body=${response.body}")
                }
            }.recover {
            case ex ⇒ logger.warn(s"error during api eligibility call, error=${ex.getMessage}")
          }

          val url =
            s"""
               |curl -v -X GET \
               |-H "Content-Type: ${params.contentType}" \
               |-H "Accept: ${params.accept}" \
               |-H "Gov-Client-User-ID: ${params.govClientUserId}" \
               |-H "Gov-Client-Timezone: ${params.govClientTimezone}" \
               |-H "Gov-Vendor-Version: ${params.govVendorVersion}" \
               |-H "Gov-Vendor-Instance-ID: ${params.govVendorInstanceId}" \
               |-H "Authorization: Bearer ${tokenCache.get("token")}" \
               |-H "Cache-Control: ${params.cacheControl}" \
               | -d '{ \
               |  "header": { \
               |    "version": ${params.version}, \
               |    "createdTimestamp": ${params.createdTimestamp}, \
               |    "clientCode": ${params.clientCode}, \
               |    "requestCorrelationId": ${params.requestCorrelationId} \
               |  }}' "${appConfig.apiUrl}/individuals/help-to-save/eligibility/${params.nino}"
               |""".stripMargin

          Future.successful(Ok(url))
      }
    )
  }

  def authorizeCallback(code: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"handling authorizeCallback from oauth")
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
