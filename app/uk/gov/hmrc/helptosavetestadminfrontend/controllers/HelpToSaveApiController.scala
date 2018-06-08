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

import com.google.common.cache._
import com.google.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector
import uk.gov.hmrc.helptosavetestadminfrontend.forms.{ContactDetails, CreateAccountForm, EligibilityRequestForm}
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.helptosavetestadminfrontend.views
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@Singleton
class HelpToSaveApiController @Inject()(http: WSHttp, authConnector: AuthConnector)(implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  val tokenCache: LoadingCache[String, String] =
    CacheBuilder
      .newBuilder
      .maximumSize(1000)
      .expireAfterWrite(3, TimeUnit.HOURS)
      .removalListener(new RemovalListener[String, String] {
        override def onRemoval(notification: RemovalNotification[String, String]): Unit = {
          logger.info(s"cache entry: (${notification.getKey}) has been removed due to ${notification.getCause}")
        }
      })
      .build(new CacheLoader[String, String] {
        override def load(key: String): String = {
          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result = Await.result(authConnector.loginAndGetToken(key), Duration(1, TimeUnit.MINUTES))
          result match {
            case Right(token) =>
              logger.info(s"Loaded access token from oauth, token=$token")
              token
            case Left(e) => throw new Exception(s"error during retrieving token from oauth, error=$e")
          }
        }
      })

  def availableFunctions(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.availableFunctions()))
  }

  def getCheckEligibilityPage(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.get_check_eligibility_page(EligibilityRequestForm.eligibilityForm)))
  }

  def checkEligibility(): Action[AnyContent] = Action.async { implicit request =>
    EligibilityRequestForm.eligibilityForm.bindFromRequest().fold(
      formWithErrors ⇒ Future.successful(Ok(views.html.get_check_eligibility_page(formWithErrors))),
      {
        params =>
          val url =
            s"""
               |curl -v -X GET \\
               |-H "Accept: ${params.accept}" \\
               |-H "Gov-Client-User-ID: ${params.govClientUserId}" \\
               |-H "Gov-Client-Timezone: ${params.govClientTimezone}" \\
               |-H "Gov-Vendor-Version: ${params.govVendorVersion}" \\
               |-H "Gov-Vendor-Instance-ID: ${params.govVendorInstanceId}" \\
               |-H "Authorization: Bearer ${tokenCache.get(params.nino)}" \\
               | "${appConfig.apiUrl}/individuals/help-to-save/eligibility/${params.nino}"
               |""".stripMargin

          Future.successful(Ok(url))
      }
    )
  }

  def getCreateAccountPage(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.get_create_account_page(CreateAccountForm.createAccountForm)))
  }

  def createAccount(): Action[AnyContent] = Action.async { implicit request =>
    CreateAccountForm.createAccountForm.bindFromRequest().fold(
      formWithErrors ⇒ Future.successful(Ok(views.html.get_create_account_page(formWithErrors))),
      {
        params =>

          val getContactDetailsJson: String = {
            val contactDetails = params.requestBody.contactDetails

            val address3 = contactDetails.address3 match {
              case Some(value) if value.trim.nonEmpty => s""""address3" : "$value","""
              case _ => ""
            }

            val address4 = contactDetails.address4 match {
              case Some(value) if value.trim.nonEmpty => s""""address4" : "$value","""
              case _ => ""
            }

            val address5 = contactDetails.address5 match {
              case Some(value) if value.trim.nonEmpty => s""""address5" : "$value","""
              case _ => ""
            }

            val countryCode = contactDetails.countryCode match {
              case Some(value) if value.trim.nonEmpty => s""""countryCode" : "$value","""
              case _ => ""
            }

            val phoneNumber = contactDetails.phoneNumber match {
              case Some(value) if value.trim.nonEmpty => s""""phoneNumber" : "$value","""
              case _ => ""
            }

            val email = contactDetails.email match {
              case Some(value) if value.trim.nonEmpty => s""""email" : "$value"""
              case _ => ""
            }

            s""" {
              "address1" : "${contactDetails.address1}",
              "address2" : "${contactDetails.address2}",
              $address3
              $address4
              $address5
              "postcode" :  "${contactDetails.postcode}",
              $countryCode
              $phoneNumber
              "communicationPreference" : "${contactDetails.communicationPreference}",
              $email"
            }""".replaceAll("(?m)^[ \t]*\r?\n", "")
          }

          val url =
            s"""
               |curl -v -X POST \\
               |-H "Content-Type: ${params.httpHeaders.contentType}" \\
               |-H "Accept: ${params.httpHeaders.accept}" \\
               |-H "Gov-Client-User-ID: ${params.httpHeaders.govClientUserId}" \\
               |-H "Gov-Client-Timezone: ${params.httpHeaders.govClientTimezone}" \\
               |-H "Gov-Vendor-Version: ${params.httpHeaders.govVendorVersion}" \\
               |-H "Gov-Vendor-Instance-ID: ${params.httpHeaders.govVendorInstanceId}" \\
               |-H "Authorization: Bearer ${tokenCache.get(params.requestBody.nino)}" \\
               | -d '{
               |  "header": {
               |    "version": "${params.requestHeaders.version}",
               |    "createdTimestamp": "${params.requestHeaders.createdTimestamp}",
               |    "clientCode": "${params.requestHeaders.clientCode}",
               |    "requestCorrelationId": "${params.requestHeaders.requestCorrelationId}"
               |  },
               |  "body": {
               |    "nino" : "${params.requestBody.nino}",
               |    "forename" : "${params.requestBody.forename}",
               |    "surname" : "${params.requestBody.surname}",
               |    "dateOfBirth" : "${params.requestBody.dateOfBirth}",
               |    "contactDetails" : $getContactDetailsJson,
               |    "registrationChannel" : "${params.requestBody.registrationChannel}"
               |  }}' "${appConfig.apiUrl}/individuals/help-to-save/account"
               |""".stripMargin

          Future.successful(Ok(url))
      }
    )
  }

  def authorizeCallback(code: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info("handling authorizeCallback from oauth")
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
