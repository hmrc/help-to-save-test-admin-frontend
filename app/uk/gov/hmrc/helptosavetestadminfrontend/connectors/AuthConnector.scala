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
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector.JsObjectOps
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.models.AuthUserDetails
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class AuthConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def loginAndGetToken(authUserDetails: AuthUserDetails)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, String]] = {
    http.post(appConfig.authStubUrl, getRequestBody(authUserDetails)).map {
      response ⇒
        response.status match {
          case Status.OK =>
            logger.info(s"Status from Auth is OK, response.body is ${response.body}")
            getGrantScopePage(response)
          case other: Int =>
            logger.info(s"Status is $other, response.body is ${response.body}")
            Future.successful(Left(s"unexpected status during auth, got status=$other but 200 expected, response body=${response.body}"))
        }
    }.recover {
      case ex ⇒ Future.successful(Left(s"error during auth, error=${ex.getMessage}"))
    }.flatMap(identity)
  }

  private def getGrantScopePage(response: HttpResponse)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, String]] = {
    val doc = Jsoup.parse(response.body)
    val oauthGrantScopeUrl = doc.getElementsByClass("button").attr("href")

    http.get(s"${appConfig.oauthURL}$oauthGrantScopeUrl", Map("Cookie" -> getMdtpCookie(response))).map {
      response ⇒
        response.status match {
          case Status.OK | Status.SEE_OTHER =>
            logger.info(s"oauth grant scope GET is successful, status = ${response.status}, body = ${response.body}")
            postGrantScope(response)
          case other: Int =>
            logger.info(s"oauth grant scope GET is failed,  is $other, response.body is ${response.body}")
            Future.successful(Left(s"oauth grant scope GET is failed, status=$other but 200 expected"))
        }
    }.recover {
      case ex ⇒ Future.successful(Left(s"error during getGrantScopePage, error=$ex"))
    }.flatMap(identity)
  }

  private def postGrantScope(response: HttpResponse)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val doc = Jsoup.parse(response.body)
    val csrfToken = doc.select("input[name=csrfToken]").attr("value")
    val authId = doc.select("input[name=auth_id]").attr("value")

    logger.info(s"auth_id is = $authId")

    val body: Map[String, Seq[String]] = Map(
      "csrfToken" → Seq(csrfToken),
      "auth_id" → Seq(authId)
    )

    val headers = Map("Cookie" -> getMdtpCookie(response),
      "Csrf-Token" -> csrfToken,
      "Content-Type" -> "application/x-www-form-urlencoded")

    http.post(s"${appConfig.oauthURL}/oauth/grantscope", body, headers).map {
      response =>
        response.status match {
          case Status.OK | Status.CREATED | Status.SEE_OTHER =>
            logger.info(s"oauth grant scope POST is successful, status = ${response.status}, body = ${response.body}")
            Right(response.body)
          case other: Int =>
            logger.info(s"oauth grant scope POST is failed,  is $other, response.body is ${response.body}")
            Left(s"oauth grant scope POST is failed, status=$other but 201 expected")
        }
    }.recover {
      case ex ⇒ Left(s"error during postGrantScope, error=$ex")
    }
  }

  private def getMdtpCookie(response: HttpResponse) =
    response.allHeaders("Set-Cookie").find(_.contains("mdtp=")).getOrElse(throw new RuntimeException("no mdtp cookie found"))

  def getRequestBody(authUserDetails: AuthUserDetails): JsValue = {
    val json: JsObject = JsObject(Map(
      "authorityId" → JsString(Random.alphanumeric.take(10).mkString),
      "affinityGroup" → JsString("Individual"),
      "confidenceLevel" → JsNumber(200),
      "credentialStrength" → JsString("strong"),
      "credentialRole" → JsString("User"),
      "redirectionUrl" → JsString(s"${appConfig.authorizeUrl}")
    ))

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
  }


}

object AuthConnector {

  implicit class JsObjectOps(val j: JsObject) extends AnyVal {
    def withField(field: String, value: Option[JsValue]): JsObject =
      value.fold(j)(v ⇒ j + (field → v))
  }

}