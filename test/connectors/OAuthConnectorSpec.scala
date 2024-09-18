/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.OAuthConnector
import uk.gov.hmrc.helptosavetestadminfrontend.models.AccessToken
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import util.WireMockMethods

import java.util.UUID
import scala.concurrent.ExecutionContext

class OAuthConnectorSpec extends AnyWordSpec with WireMockMethods with WireMockSupport with GuiceOneAppPerSuite {

  val (desBearerToken, desEnvironment) = "token" -> "environment"

  private val config = Configuration(
    ConfigFactory.parseString(
      s"""
         |oauth-access-token-url = "http://$wireMockHost:$wireMockPort"
         |microservice {
         |  services {
         |    oauth-frontend {
         |      client_id = "abdc1234"
         |      client_secret = "secret"
         |    }
         |    help-to-save-test-admin-frontend {
         |      url: "http://localhost:7009"
         |    }
         |  }
         |}
         |
         |privileged-access {
         |  client-id = abdc1234
         |  totp-secret = secret
         |}
         |""".stripMargin
    )
  )

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(config).build()

  val connector: OAuthConnector = app.injector.instanceOf[OAuthConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val emptyJsonBody = "{}"

  "The OAuthConnector" when {

    "successfully get Access token Restricted" in {
      val token: String = "token13579"
      val authCode = "auth123"

      val httpResponse = HttpResponse(OK, Json.parse(s"""{"access_token":"$token"}"""), Map.empty[String, Seq[String]])
      val uuid = Some(UUID.randomUUID())

      val redirectUri = s"http://localhost:7009/help-to-save-test-admin-frontend/authorize-callback?id=${uuid.get}"

      val json =
        Json.parse(s"""{
          "client_secret":"secret",
          "client_id":"abdc1234",
          "grant_type":"authorization_code",
          "redirect_uri": "$redirectUri",
          "code":"$authCode"
      }""")

      when(
        POST,
        "/",
        headers = Map.empty,
        body = Some(json.toString())
      ) thenReturn (httpResponse.status, httpResponse.body)
      await(connector.getAccessTokenUserRestricted(authCode, uuid, Map.empty)) shouldBe Right(AccessToken(token))
    }

    "verify error status codes for get Access token Restricted" in {
      val authCode = "auth123"
      val uuid = Some(UUID.randomUUID())
      val redirectUri = s"http://localhost:7009/help-to-save-test-admin-frontend/authorize-callback?id=${uuid.get}"
      val json =
        Json.parse(s"""{
          "client_secret":"secret",
          "client_id":"abdc1234",
          "grant_type":"authorization_code",
          "redirect_uri": "$redirectUri",
          "code":"$authCode"
      }""")

      List(
        HttpResponse(400, emptyJsonBody),
        HttpResponse(401, emptyJsonBody),
        HttpResponse(403, emptyJsonBody),
        HttpResponse(500, emptyJsonBody),
        HttpResponse(502, emptyJsonBody),
        HttpResponse(503, emptyJsonBody)
      ).foreach { httpResponse =>
        when(
          POST,
          "/",
          headers = Map.empty,
          body = Some(json.toString())
        ) thenReturn (httpResponse.status, httpResponse.body)

        await(connector.getAccessTokenUserRestricted(authCode, uuid, Map.empty)) shouldBe Left(
          s"Got status ${httpResponse.status}, body was ${httpResponse.body}"
        )
      }
    }

    "successfully get Access token Privileged" in {
      val token: String = "token13579"
      val totpCode = "tot123"
      val json =
        Json.parse(s"""{
          "client_secret":"$totpCode",
          "client_id":"abdc1234",
          "grant_type":"client_credentials"
      }""")

      val httpResponse = HttpResponse(OK, Json.parse(s"""{"access_token":"$token"}"""), Map.empty[String, Seq[String]])

      when(
        POST,
        "/",
        headers = Map.empty,
        body = Some(json.toString())
      ) thenReturn (httpResponse.status, httpResponse.body)
      await(connector.getAccessTokenPrivileged(totpCode, Map.empty)) shouldBe Right(AccessToken(token))
    }

    "verify error status codes for get Access token Privileged" in {
      val totpCode = "tot123"
      val json =
        Json.parse(s"""{
          "client_secret":"$totpCode",
          "client_id":"abdc1234",
          "grant_type":"client_credentials"
      }""")
      List(
        HttpResponse(400, emptyJsonBody),
        HttpResponse(401, emptyJsonBody),
        HttpResponse(403, emptyJsonBody),
        HttpResponse(500, emptyJsonBody),
        HttpResponse(502, emptyJsonBody),
        HttpResponse(503, emptyJsonBody)
      ).foreach { httpResponse =>
        when(
          POST,
          "/",
          headers = Map.empty,
          body = Some(json.toString())
        ) thenReturn (httpResponse.status, httpResponse.body)

        await(connector.getAccessTokenPrivileged(totpCode, Map.empty)) shouldBe Left(
          s"Got status ${httpResponse.status}, body was ${httpResponse.body}"
        )
      }
    }
  }

}
