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
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def loginAndGetToken(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, String]] = {
    http.post(appConfig.authStubUrl, Json.parse(getRequestBody(nino))).map {
      response ⇒
        response.status match {
          case Status.OK =>
            Right(response.body)
          case other: Int => Left(s"unexpected status during auth, got status=$other but 200 expected, response body=${response.body}")
        }
    }.recover {
      case ex ⇒ Left(s"error during auth, error=${ex.getMessage}")
    }
  }

  def getRequestBody(nino: String): String =
    s"""{
         "authorityId":"htsapi",
         "affinityGroup":"Individual",
         "confidenceLevel":200,
         "credentialStrength":"strong",
         "nino":"$nino",
         "credentialRole":"User",
         "email":"test@user.com",
         "redirectionUrl":"${appConfig.authorizeUrl}"
        }""".stripMargin

}

