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
import play.api.libs.json._
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def loginAndGetToken(nino: Option[String])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, String]] = {
    http.post(appConfig.authStubUrl, getRequestBody(nino)).map {
      response ⇒
        response.status match {
          case Status.OK =>
            logger.info(s"Status is OK, response.body is ${response.body}")
            Right(response.body)
          case other: Int =>
            logger.info(s"Status is $other, response.body is ${response.body}")
            Left(s"unexpected status during auth, got status=$other but 200 expected, response body=${response.body}")
        }
    }.recover {
      case ex ⇒ Left(s"error during auth, error=${ex.getMessage}")}
  }

  def getRequestBody(nino: Option[String]): JsValue ={
    val json: JsObject = JsObject(Map(
      "authorityId" → JsString("htsapi"),
      "affinityGroup" → JsString("Individual"),
      "confidenceLevel" → JsNumber(200),
      "credentialStrength" → JsString("strong"),
      "credentialRole" → JsString("User"),
      "email" → JsString("test@user.com"),
      "redirectionUrl" → JsString(s"${appConfig.authorizeUrl}")
    ))
    nino.fold(json)(n ⇒ json + ("nino" → JsString(n)))
  }

}