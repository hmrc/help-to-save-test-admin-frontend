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

package uk.gov.hmrc.helptosavetestadminfrontend.connectors

import com.google.inject.Inject
import play.api.libs.json.Json
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.{AccessType, Logging, Privileged, UserRestricted}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.http.Status._
import uk.gov.hmrc.helptosavetestadminfrontend.models.AccessToken

import scala.concurrent.{ExecutionContext, Future}

class OAuthConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def getAccessToken(authorisationCode: String, accessType: AccessType, extraHeaders: Map[String, String])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, AccessToken]] = {
    http.post("http://oauth-frontend.public.mdtp:80/oauth/token", Json.parse(tokenRequest(authorisationCode, accessType)), extraHeaders)
      .map[Either[String, AccessToken]]{
      response =>
        response.status match {
          case OK =>
            (response.json \ "access_token").validate[String].fold(
              errors ⇒ Left(s"An error occurred during token validation: $errors"),
              token ⇒ Right(AccessToken(token))
            )
          case other: Int =>
            Left(s"Got status $other, body was ${response.body}")
        }
    }.recover {
      case ex ⇒
        Left(ex.getMessage)
    }
  }

  def tokenRequest(code: String, accessType: AccessType): String ={
    accessType match {
      case UserRestricted ⇒
        s"""{
          "client_secret":"${appConfig.clientSecret}",
          "client_id":"${appConfig.clientId}",
          "grant_type":"authorization_code",
          "redirect_uri":"${appConfig.authorizeCallback()}",
          "code":"$code"
      }"""

      case Privileged     ⇒
        s"""{
          "client_secret":"$code",
          "client_id":"${appConfig.privilegedAccessClientId}",
          "grant_type":"client_credentials"
      }"""

    }
  }


}
