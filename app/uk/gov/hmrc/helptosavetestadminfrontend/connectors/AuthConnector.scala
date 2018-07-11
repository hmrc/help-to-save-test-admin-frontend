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
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector.JsObjectOps
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.models.AuthUserDetails
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class AuthConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def loginAndGetToken(authUserDetails: Option[AuthUserDetails])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, String]] = {
    logger.info(s"Auth user details from the form are = $authUserDetails")
    http.post(appConfig.authStubUrl, getRequestBody(authUserDetails)).map {
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

  def getRequestBody(authUserDetails: Option[AuthUserDetails]): JsValue = {
    val json: JsObject = JsObject(Map(
      "authorityId" → JsString(Random.alphanumeric.take(10).mkString),
      "affinityGroup" → JsString("Individual"),
      "confidenceLevel" → JsNumber(200),
      "credentialStrength" → JsString("strong"),
      "credentialRole" → JsString("User"),
      "redirectionUrl" → JsString(s"${appConfig.authorizeUrl}")
    ))

    json
      .withField("nino", authUserDetails.flatMap(_.nino.map(JsString)))
      .withField("itmp.givenName", authUserDetails.flatMap(_.forename.map(JsString)))
      .withField("itmp.familyName", authUserDetails.flatMap(_.surname.map(JsString)))
      .withField("itmp.dateOfBirth", authUserDetails.flatMap(_.dateOfBirth.map(JsString)))
      .withField("itmp.address.line1", authUserDetails.flatMap(_.address1.map(JsString)))
      .withField("itmp.address.line2", authUserDetails.flatMap(_.address2.map(JsString)))
      .withField("itmp.address.line3", authUserDetails.flatMap(_.address3.map(JsString)))
      .withField("itmp.address.line4", authUserDetails.flatMap(_.address4.map(JsString)))
      .withField("itmp.address.line5", authUserDetails.flatMap(_.address5.map(JsString)))
      .withField("itmp.address.postCode", authUserDetails.flatMap(_.postcode.map(JsString)))
      .withField("itmp.address.countryCode", authUserDetails.flatMap(_.countryCode.map(JsString)))
      .withField("email", authUserDetails.flatMap(_.email.map(JsString)))
  }


}

object AuthConnector {

  implicit class JsObjectOps(val j: JsObject) extends AnyVal {
    def withField(field: String, value: Option[JsValue]): JsObject =
      value.fold(j)(v ⇒ j + (field → v))
  }

}