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
import org.apache.pekko.util.Helpers.Requiring
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString, JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.AuthConnector
import uk.gov.hmrc.helptosavetestadminfrontend.models.{AuthUserDetails, LocalPrivilegedToken}
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import util.WireMockMethods

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class AuthConnectorSpec extends AnyWordSpec with WireMockMethods with WireMockSupport with GuiceOneAppPerSuite {

  val (desBearerToken, desEnvironment) = "token" -> "environment"

  private val config = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |      auth-login-api {
         |      url = "http://$wireMockHost:$wireMockPort"
         |    }
         |      auth {
         |      url = "http://$wireMockHost:$wireMockPort"
         |    }
         |  }
         |}
         |""".stripMargin
    )
  )

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(config).build()

  val connector: AuthConnector = app.injector.instanceOf[AuthConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val emptyJsonBody = "{}"

  "The AuthConnector" when {

    "log in " must {

      "successful log in" in {

        val authUserDetails = AuthUserDetails.empty()
        val token: String = "token13579"

        val headers = Map(HeaderNames.AUTHORIZATION -> "auth1", HeaderNames.LOCATION -> "loc1")
        val httpResponse =
          HttpResponse(201, Json.parse(s"""{"gatewayToken":"$token"}"""), Map.empty[String, Seq[String]])

        when(
          POST,
          "/government-gateway/session/login"
        ) thenReturn (httpResponse.status, headers, httpResponse.body)

        val result = await(connector.login(authUserDetails)).value
        result.value.toOption.get.session.get("authToken").get shouldBe "auth1"

      }

      "Error for missing auth header" in {

        val authUserDetails = AuthUserDetails.empty()
        val token: String = "token13579"

        val headers = Map(HeaderNames.LOCATION -> "loc1")
        val httpResponse =
          HttpResponse(201, Json.parse(s"""{"gatewayToken":"$token"}"""), Map.empty[String, Seq[String]])

        when(
          POST,
          "/government-gateway/session/login"
        ) thenReturn (httpResponse.status, headers, httpResponse.body)

        await(connector.login(authUserDetails)).value shouldBe Left(
          "Internal Error, missing headers or gatewayToken in response from auth-login-api"
        )

      }

      "Error for missing location header" in {

        val authUserDetails = AuthUserDetails.empty()
        val token: String = "token13579"

        val headers = Map(HeaderNames.AUTHORIZATION -> "auth1")
        val httpResponse =
          HttpResponse(201, Json.parse(s"""{"gatewayToken":"$token"}"""), Map.empty[String, Seq[String]])

        when(
          POST,
          "/government-gateway/session/login"
        ) thenReturn (httpResponse.status, headers, httpResponse.body)

        await(connector.login(authUserDetails)).value shouldBe Left(
          "Internal Error, missing headers or gatewayToken in response from auth-login-api"
        )

      }

      "Error for missing gateway token" in {

        val authUserDetails = AuthUserDetails.empty()

        val headers = Map(HeaderNames.AUTHORIZATION -> "auth1", HeaderNames.LOCATION -> "loc1")
        val httpResponse = HttpResponse(201, emptyJsonBody, Map.empty[String, Seq[String]])

        when(
          POST,
          "/government-gateway/session/login"
        ) thenReturn (httpResponse.status, headers, httpResponse.body)

        await(connector.login(authUserDetails)).value shouldBe Left(
          "Internal Error, missing headers or gatewayToken in response from auth-login-api"
        )

      }

      "verify error status codes" in {
        List(
          HttpResponse(400, emptyJsonBody),
          HttpResponse(401, emptyJsonBody),
          HttpResponse(403, emptyJsonBody),
          HttpResponse(500, emptyJsonBody),
          HttpResponse(502, emptyJsonBody),
          HttpResponse(503, emptyJsonBody)
        ).foreach { httpResponse =>
          val authUserDetails = AuthUserDetails.empty()

          val headers = Map(HeaderNames.AUTHORIZATION -> "auth1", HeaderNames.LOCATION -> "loc1")

          when(
            POST,
            "/government-gateway/session/login"
          ) thenReturn (httpResponse.status, headers, httpResponse.body)

          await(connector.login(authUserDetails)).value shouldBe Left(
            s"failed calling auth-login-api, got status ${httpResponse.status}, body: ${httpResponse.body}"
          )
        }
      }
    }

    "getPrivilegedToken" must {
      val privilegedRequestBody: JsValue = JsObject(
        Map(
          "clientId"   -> JsString("id"),
          "enrolments" -> JsArray(),
          "ttl"        -> JsNumber(1200) // scalastyle:ignore magic.number
        )
      )

      "returns token successfully" in {

        val token = "token123"

        val headers = Map(HeaderNames.AUTHORIZATION -> token)

        val httpResponse: HttpResponse = HttpResponse(200, emptyJsonBody, Map.empty[String, Seq[String]])

        when(
          method = POST,
          uri = "/auth/sessions",
          body = Some(privilegedRequestBody.toString())
        ) thenReturn (httpResponse.status, headers, httpResponse.body)

        await(connector.getPrivilegedToken()).value shouldBe Right(LocalPrivilegedToken(token))
      }

      "Error for missing auth header" in {

        val httpResponse: HttpResponse = HttpResponse(200, emptyJsonBody, Map.empty[String, Seq[String]])

        when(
          method = POST,
          uri = "/auth/sessions",
          body = Some(privilegedRequestBody.toString())
        ) thenReturn (httpResponse.status, httpResponse.body)

        await(connector.getPrivilegedToken()).value shouldBe Left("Could not find Authorization header in response")

      }

      "verify error status codes" in {
        List(
          HttpResponse(400, emptyJsonBody),
          HttpResponse(401, emptyJsonBody),
          HttpResponse(403, emptyJsonBody),
          HttpResponse(500, emptyJsonBody),
          HttpResponse(502, emptyJsonBody),
          HttpResponse(503, emptyJsonBody)
        ).foreach { httpResponse =>
          when(
            method = POST,
            uri = "/auth/sessions",
            body = Some(privilegedRequestBody.toString())
          ) thenReturn (httpResponse.status, httpResponse.body)

          await(connector.getPrivilegedToken()).value shouldBe Left("Could not find Authorization header in response")
        }
      }

    }

  }
}
