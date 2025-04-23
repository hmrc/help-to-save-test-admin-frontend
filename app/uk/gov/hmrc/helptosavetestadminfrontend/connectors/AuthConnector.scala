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

import cats.data.NonEmptyList
import com.google.inject.Inject
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.Session
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector.JsObjectOps
import uk.gov.hmrc.helptosavetestadminfrontend.models._
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReadsInstances.readEitherOf
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId, SessionKeys, StringContextOps, UpstreamErrorResponse}

import java.time.Instant
import java.util.UUID
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class AuthConnector @Inject() (http: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends Logging {
  def login(
    authUserDetails: AuthUserDetails
  )(implicit hc: HeaderCarrier): Future[Either[String, SessionToken]] = {
    val credId = Random.alphanumeric.take(10).mkString // scalastyle:ignore magic.number
    val json = getGGRequestBody(authUserDetails, credId)
    http
      .post(url"${appConfig.authLoginApiUrl}/government-gateway/session/login")
      .withBody(json)
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Left(UpstreamErrorResponse(message, statusCode, _, _)) =>
          Left(s"failed calling auth-login-api, got status $statusCode, message: $message")
        case Right(HttpResponse(status, body, headers)) =>
          if (status == 201) {
            (
              headers.get(HeaderNames.AUTHORIZATION),
              headers.get(HeaderNames.LOCATION),
              (Json.parse(body) \ "gatewayToken").asOpt[String]
            ) match {
              case (Some(token), Some(_), Some(_)) =>
                val session = Session(
                  Map(
                    SessionKeys.sessionId            -> SessionId(s"session-${UUID.randomUUID}").value,
                    SessionKeys.authToken            -> token.head,
                    SessionKeys.lastRequestTimestamp -> Instant.now().toEpochMilli.toString
                  )
                )

                Right(SessionToken(session))
              case _ =>
                Left("Internal Error, missing headers or gatewayToken in response from auth-login-api")
            }
          } else {
            Left(s"failed calling auth-login-api, got status $status, body: $body")
          }
      }
      .recover { case ex =>
        Left(s"error during auth, error=${ex.getMessage}")
      }
  }

  def getPrivilegedToken()(implicit
    hc: HeaderCarrier
  ): Future[Either[String, LocalPrivilegedToken]] =
    http
      .post(url"${appConfig.authUrl}/auth/sessions")
      .withBody(privilegedRequestBody)
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Left(_) => Left("Could not find Authorization header in response")
        case Right(HttpResponse(_, _, headers)) =>
          headers
            .get(HeaderNames.AUTHORIZATION)
            .fold[Either[String, LocalPrivilegedToken]](
              Left("Could not find Authorization header in response")
            )(t => Right(LocalPrivilegedToken(t.head)))
      }

  private val privilegedRequestBody = JsObject(
    Map(
      "clientId"   -> JsString("id"),
      "enrolments" -> JsArray(),
      "ttl"        -> JsNumber(1200) // scalastyle:ignore magic.number
    )
  )

  private def getGGRequestBody(authUserDetails: AuthUserDetails, credId: String) = {
    val json: JsObject = JsObject(
      Map(
        "credId"             -> JsString(credId),
        "affinityGroup"      -> JsString("Individual"),
        "confidenceLevel"    -> JsNumber(200), // scalastyle:ignore magic.number
        "credentialStrength" -> JsString("strong"),
        "credentialRole"     -> JsString("User")
      )
    )

    json
      .withField("nino", authUserDetails.nino.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "givenName"), authUserDetails.forename.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "familyName"), authUserDetails.surname.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "birthdate"), authUserDetails.dateOfBirth.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "address", "line1"), authUserDetails.address1.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "address", "line2"), authUserDetails.address2.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "address", "line3"), authUserDetails.address3.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "address", "line4"), authUserDetails.address4.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "address", "line5"), authUserDetails.address5.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "address", "postCode"), authUserDetails.postcode.map(JsString.apply))
      .withField(NonEmptyList.of("itmpData", "address", "countryCode"), authUserDetails.countryCode.map(JsString.apply))
      .withField("email", authUserDetails.email.map(JsString.apply))
      .withField("enrolments", Some(JsArray()))
  }
}

object AuthConnector {
  implicit class JsObjectOps(val j: JsObject) extends AnyVal {
    def withField(path: NonEmptyList[String], value: Option[JsValue]): JsObject =
      value.fold(j)(v => j.deepMerge(jsObject(path -> v)))

    def withField(fieldName: String, value: Option[JsValue]): JsObject =
      withField(NonEmptyList.one(fieldName), value)
  }

  private def jsObject(s: (NonEmptyList[String], JsValue)): JsObject = {
    @tailrec
    def loop(l: List[String], acc: JsObject): JsObject = l match {
      case Nil          => acc
      case head :: Nil  => JsObject(List(head -> acc))
      case head :: tail => loop(tail, JsObject(List(head -> acc)))
    }

    val reversed = s._1.reverse
    loop(reversed.tail, JsObject(List(reversed.head -> s._2)))
  }
}
