/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.models.AccessToken
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReadsInstances.readEitherOf
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class OAuthConnector @Inject() (http: HttpClientV2, appConfig: AppConfig, config: Configuration)(implicit
  ec: ExecutionContext
) extends Logging {
  private def getAccessToken(body: JsValue, extraHeaders: Map[String, String])(implicit
    hc: HeaderCarrier
  ): Future[Either[String, AccessToken]] =
    http
      .post(url"${config.underlying.getString("oauth-access-token-url")}")
      .withBody(body)
      .setHeader(extraHeaders.toSeq: _*)
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Left(UpstreamErrorResponse(message, statusCode, _, _)) =>
          Left(s"Got status $statusCode, body was $message")
        case Right(HttpResponse(status, body, _)) =>
          status match {
            case OK =>
              (Json.parse(body) \ "access_token")
                .validate[String]
                .fold(
                  errors => Left(s"An error occurred during token validation: $errors"),
                  token => Right(AccessToken(token))
                )
            case other: Int =>
              Left(s"Got status $other, body was $body")
          }
      }
      .recover { case ex =>
        Left(ex.getMessage)
      }

  def getAccessTokenUserRestricted(
    authorisationCode: String,
    id: Option[UUID],
    extraHeaders: Map[String, String]
  )(implicit hc: HeaderCarrier): Future[Either[String, AccessToken]] = {
    val json =
      Json.parse(s"""{
          "client_secret":"${appConfig.clientSecret}",
          "client_id":"${appConfig.clientId}",
          "grant_type":"authorization_code",
          "redirect_uri":"${appConfig.authorizeCallback(id)}",
          "code":"$authorisationCode"
      }""")
    getAccessToken(json, extraHeaders)
  }

  def getAccessTokenPrivileged(totpCode: String, extraHeaders: Map[String, String])(implicit
    hc: HeaderCarrier
  ): Future[Either[String, AccessToken]] = {
    val json =
      Json.parse(s"""{
          "client_secret":"$totpCode",
          "client_id":"${appConfig.privilegedAccessClientId}",
          "grant_type":"client_credentials"
      }""")
    getAccessToken(json, extraHeaders)
  }
}
