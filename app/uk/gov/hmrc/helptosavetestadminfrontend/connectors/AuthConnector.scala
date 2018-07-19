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

import java.util.UUID

import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.mvc.Session
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector.JsObjectOps
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.models.{AuthUserDetails, BearerTokenStuff, Token}
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class AuthConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def login(authUserDetails: AuthUserDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, Token]] = {
    val credId = Random.alphanumeric.take(10).mkString
    http.post(appConfig.authLoginApiUrl, getRequestBody(authUserDetails, credId)).map {
      response ⇒
        logger.info(s"all headers = ${response.allHeaders}")
        if (response.status == 201) {
          (
            response.header(HeaderNames.AUTHORIZATION),
            response.header(HeaderNames.LOCATION),
            (response.json \ "gatewayToken").asOpt[String]
          ) match {
            case (Some(token), Some(sessionUri), Some(receivedGatewayToken)) =>

              val session = Session(Map(
                SessionKeys.sessionId -> SessionId(s"session-${UUID.randomUUID}").value,
                SessionKeys.authProvider -> "GGW",
                SessionKeys.userId -> sessionUri,
                SessionKeys.authToken -> token,
                SessionKeys.lastRequestTimestamp -> DateTime.now.getMillis.toString,
                SessionKeys.token -> receivedGatewayToken,
                SessionKeys.affinityGroup -> "Individual",
                SessionKeys.name -> credId
              ))

              Right(BearerTokenStuff(session))
            case _ => Left("Internal Error, missing headers or gatewayToken in response from auth-login-api")
          }
        } else {
          Left(s"failed calling auth-login-api , expected, but got ${response.status}, body: ${response.body}")
        }
    }.recover {
      case ex ⇒ Left(s"error during auth, error=${ex.getMessage}")
    }
  }

  def getRequestBody(authUserDetails: AuthUserDetails, credId: String): JsValue = {
    val json: JsObject = JsObject(Map(
      "credId" → JsString(credId),
      "affinityGroup" → JsString("Individual"),
      "confidenceLevel" → JsNumber(200),
      "credentialStrength" → JsString("strong"),
      "credentialRole" → JsString("User"))
    )

    json
      .withField("nino", authUserDetails.nino.map(JsString))
      .withField("itmp.givenName", authUserDetails.forename.map(JsString))
      .withField("itmp.familyName", authUserDetails.surname.map(JsString))
      .withField("itmp.dateOfBirth", authUserDetails.dateOfBirth.map(JsString))
      .withField("itmp.address.line1", authUserDetails.address1.map(JsString))
      .withField("itmp.address.line2", authUserDetails.address2.map(JsString))
      .withField("itmp.address.line3", authUserDetails.address3.map(JsString))
      .withField("itmp.address.line4", authUserDetails.address4.map(JsString))
      .withField("itmp.address.line5", authUserDetails.address5.map(JsString))
      .withField("itmp.address.postCode", authUserDetails.postcode.map(JsString))
      .withField("itmp.address.countryCode", authUserDetails.countryCode.map(JsString))
      .withField("email", authUserDetails.email.map(JsString))
      .withField("enrolments", Some(JsArray()))
  }


}

object AuthConnector {

  implicit class JsObjectOps(val j: JsObject) extends AnyVal {
    def withField(field: String, value: Option[JsValue]): JsObject =
      value.fold(j)(v ⇒ j + (field → v))
  }

}