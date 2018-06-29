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
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.HeaderCarrier
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}

class OAuthConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def requestPrivilegedAccess(totpCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, String]] = {
    http.post(s"${appConfig.oauthURL}/oauth/token", Json.parse(appConfig.tokenRequest(totpCode))).map {
      response =>
        response.status match {
          case OK =>
            val token = (response.json \ "access_token").as[String]
            Right(token)
          case other: Int =>
            logger.warn(s"got $other status during get access_token, body=${response.body}")
            Left("An error occurred, a token was not returned")
        }
    }.recover {
      case ex ⇒
        logger.warn(s"error during /oauth/token, error=${ex.getMessage}")
        Left("A server error occurred")
    }
  }


}
