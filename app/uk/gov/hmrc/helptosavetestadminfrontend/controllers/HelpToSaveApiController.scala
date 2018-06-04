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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging

import scala.concurrent.Future

class HelpToSaveApiController @Inject()(http: WSHttp)(implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  val clientId: String = appConfig.getString("microservice.services.oauth-frontend.client_id")
  val clientSecret: String = appConfig.getString("microservice.services.oauth-frontend.client_secret")

  val adminFrontendHost: String = appConfig.getString("microservice.services.host")

  val eligibilityAuthorizeCallback = s"$adminFrontendHost/help-to-save-test-admin-frontend/eligibility-authorize-callback"

  val createAccountAuthorizeCallback = s"$adminFrontendHost/help-to-save-test-admin-frontend/create-account-authorize-callback"

  val apiHost: String = appConfig.baseUrl("api")

  val oauthURL: String = appConfig.baseUrl("oauth-frontend")

  val oauthTokenCallback = s"$adminFrontendHost/help-to-save-test-admin-frontend/handle-oauth-token-callback"

  val eligibilityScope = "read:help-to-save"
  val createAccountScope = "write:help-to-save"

  var eligibilityAccessToken = ""
  var createAccountAccessToken = ""

  def authLoginStubEligibilityCallback: Action[AnyContent] = Action.async { implicit request =>
    val url = s"/oauth/authorize?client_id=$clientId&response_type=code&scope=$eligibilityScope&redirect_uri=$eligibilityAuthorizeCallback"
    Future.successful(SeeOther(url))
  }

  def handleEligibilityAuthorizeCallback: Action[AnyContent] = Action.async { implicit request =>
    val b = eligibilityBody(request.queryString.get("code"))
    http.post(s"$oauthURL/oauth/token", Json.parse(b), Map("Content-Type" -> "application/json"))
      .map {
        response =>
          response.status match {
            case OK | CREATED =>
              eligibilityAccessToken = (response.json \ "access_token").as[String]
              Ok("saved access_token")
            case other: Int =>
              logger.warn(s"got $other status during get access_token, body=${response.body}")
              InternalServerError
          }
      }
  }

  def handleEligibilityOauthTokenCallback(): Action[AnyContent] = Action.async { implicit request =>

    val url =
      s"""
         |curl -v -X GET
         |-H "Content-Type: application/json"
         |-H "Accept: application/vnd.hmrc.2.0+json"
         |-H "Gov-Client-User-ID: PROVIDE_NINO"
         |-H "Gov-Client-Timezone: UTC"
         |-H "Gov-Vendor-Version: 1.3"
         |-H "Gov-Vendor-Instance-ID: ${UUID.randomUUID().toString}"
         |-H "Authorization: Bearer $eligibilityAccessToken"
         |-H "Cache-Control: no-cache"
         |-H "Postman-Token: ${UUID.randomUUID().toString}"
         | -d '{
         |  "header": {
         |    "version": "1.0",
         |    "createdTimestamp": "2017-11-22 23:11:09 GMT",
         |    "clientCode": "KCOM",
         |    "requestCorrelationId": "${UUID.randomUUID().toString}"
         |  }}' "$apiHost/individuals/help-to-save/eligibility/PROVIDE_NINO_HERE"
         |
       """.stripMargin
    Future.successful(Ok(url))
  }

  def eligibilityBody(maybeCode: Option[Seq[String]]): String =
    s"""{
          "client_secret":"$clientSecret",
          "client_id":"$clientId",
          "grant_type":"authorization_code",
          "redirect_uri":"$adminFrontendHost/help-to-save-test-admin-frontend/eligibility-authorize-callback",
          "code":"${maybeCode.getOrElse(Seq("")).head}"
      }"""




  def authLoginStubCreateAccountCallback: Action[AnyContent] = Action.async { implicit request =>
    val url = s"/oauth/authorize?client_id=$clientId&response_type=code&scope=$createAccountScope&redirect_uri=$createAccountAuthorizeCallback"
    Future.successful(SeeOther(url))
  }

  def handleCreateAccountAuthorizeCallback: Action[AnyContent] = Action.async { implicit request =>
    val b = createAccountBody(request.queryString.get("code"))
    http.post(s"$oauthURL/oauth/token", Json.parse(b), Map("Content-Type" -> "application/json"))
      .map {
        response =>
          response.status match {
            case OK | CREATED =>
              createAccountAccessToken = (response.json \ "access_token").as[String]
              Ok("saved access_token")
            case other: Int =>
              logger.warn(s"got $other status during get access_token for create_account, body=${response.body}")
              InternalServerError
          }
      }
  }

  def handleCreateAccountOauthTokenCallback(): Action[AnyContent] = Action.async { implicit request =>

    val url =
      s"""
         |curl -v -X POST
         |-H "Content-Type: application/json"
         |-H "Accept: application/vnd.hmrc.1.0+json"
         |-H "Gov-Client-User-ID: EL069651A"
         |-H "Gov-Client-Timezone: UTC"
         |-H "Gov-Vendor-Version: 1.3"
         |-H "Gov-Vendor-Instance-ID: ${UUID.randomUUID().toString}"
         |-H "Authorization: Bearer $createAccountAccessToken"
         |-H "Cache-Control: no-cache"
         | -d '{
         |  "header": {
         |     "version": "1.0",
         |     "createdTimestamp": "2018-01-22 23:11:09 GMT",
         |     "clientCode": "KCOM",
         |     "requestCorrelationId": "${UUID.randomUUID().toString}"
         |    },
         |  "body": {
         |     "nino": "PROVIDE_NINO",
         |     "forename": "Alex",
         |     "surname": "Lillitwinkle",
         |     "dateOfBirth": "19920423",
         |     "contactDetails": {
         |         "address1": "86 Ashopton Road",
         |         "address2": "Blackpool",
         |         "postcode": "FY43 1FB",
         |         "countryCode": "GB",
         |         "communicationPreference": "00"
         |       },
         |     "registrationChannel": "callCentre"
         |   }
         | }' "$apiHost/individuals/help-to-save/account"
       """.stripMargin
    Future.successful(Ok(url))
  }

  def createAccountBody(maybeCode: Option[Seq[String]]): String =
    s"""{
          "client_secret":"$clientSecret",
          "client_id":"$clientId",
          "grant_type":"authorization_code",
          "redirect_uri":"$adminFrontendHost/help-to-save-test-admin-frontend/create-account-authorize-callback",
          "code":"${maybeCode.getOrElse(Seq("")).head}"
      }"""
}
