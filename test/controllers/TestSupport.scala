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

package controllers

import com.typesafe.config.ConfigFactory
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Suite}
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.{Application, Configuration, Play}
import play.filters.csrf.CSRFAddToken
import uk.gov.hmrc.helptosavetestadminfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.helptosavetestadminfrontend.views.html._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import java.util.UUID
import scala.concurrent.ExecutionContext

trait TestSupport
    extends UnitSpec
    with BeforeAndAfterAll
    with ScalaFutures
    with MockitoSugar {
  this: Suite =>

  lazy val additionalConfig = Configuration()

  def buildFakeApplication(additionalConfig: Configuration): Application =
    new GuiceApplicationBuilder()
      .configure(
        Configuration(
          ConfigFactory.parseString("""
                                      |
                                      |
          """.stripMargin)
        ).withFallback(additionalConfig))
      .build()

  implicit lazy val fakeApplication: Application = buildFakeApplication(
    additionalConfig)

  implicit lazy val ec: ExecutionContext =
    fakeApplication.injector.instanceOf[ExecutionContext]

  implicit val headerCarrier: HeaderCarrier =
    HeaderCarrier(sessionId = Some(SessionId(UUID.randomUUID().toString)))

  override def beforeAll(): Unit = {
    Play.start(fakeApplication)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    Play.stop(fakeApplication)
    super.afterAll()
  }

  val testMCC: MessagesControllerComponents =
    fakeApplication.injector.instanceOf[MessagesControllerComponents]

  implicit val messagesApi: MessagesApi =
    fakeApplication.injector.instanceOf[MessagesApi]

  implicit lazy val appConfig: AppConfig =
    fakeApplication.injector.instanceOf[AppConfig]
  implicit lazy val specify_emails_to_delete: specify_emails_to_delete =
    fakeApplication.injector.instanceOf[specify_emails_to_delete]
  implicit lazy val emails_deleted: emails_deleted =
    fakeApplication.injector.instanceOf[emails_deleted]

  implicit lazy val configuration: Configuration =
    appConfig.runModeConfiguration

  val fakeRequest: FakeRequest[_] = FakeRequest("GET", "/")

  val csrfAddToken: CSRFAddToken =
    fakeApplication.injector.instanceOf[play.filters.csrf.CSRFAddToken]

  lazy val testErrorHandler: ErrorHandler =
    fakeApplication.injector.instanceOf[ErrorHandler]
}
