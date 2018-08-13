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

package uk.gov.hmrc.helptosavetestadminfrontend.controllers

import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.{AuthConnectorForITests, HelpToSaveApiConnector, OAuthConnector}
import uk.gov.hmrc.helptosavetestadminfrontend.models.{AccessToken, AuthUserDetails}
import uk.gov.hmrc.helptosavetestadminfrontend.util.{Logging, UserRestricted}

import scala.concurrent.Future

class ApiIntegrationTestController @Inject()(oauthConnector: OAuthConnector,
                                             authConnectorForITests: AuthConnectorForITests,
                                             helpToSaveApiConnector: HelpToSaveApiConnector)
                                            (implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {


  def generateOAuthTokenForITests(nino: String): Action[AnyContent] = Action.async { implicit request ⇒
    authConnectorForITests.loginAndGetToken(AuthUserDetails(Some(nino), Some("forename"), Some("surname"), Some("1992-04-23"), None, None, None, None, None, None, None, None))
      .map {
        case Right(token) ⇒
          Ok(token)
        case Left(error) ⇒
          logger.warn(s"Could not get token: $error")
          internalServerError()
      }
  }

  def oAuthCallbackForITests(code: String): Action[AnyContent] = Action.async { implicit request ⇒
    logger.info("handling oAuthCallback for IT tests from oauth")

    val cookies = request.headers.toMap.get("Cookie").flatMap(_.headOption).getOrElse(throw new RuntimeException("no Cookie found in the headers"))

    oauthConnector.getAccessToken(code, Some("ITest"), UserRestricted, Map("Cookie" -> cookies)).map {
      case Right(AccessToken(token)) ⇒
        Ok(token)
      case Left(error) ⇒
        logger.warn(s"Could not get token: $error")
        internalServerError()
    }
  }

  def getEligibilityForITests(nino: String): Action[AnyContent] = Action.async { implicit request ⇒
    authConnectorForITests.loginAndGetToken(AuthUserDetails(Some(nino), Some("forename"), Some("surname"), Some("1992-04-23"), None, None, None, None, None, None, None, None))
      .map {
        case Right(token) ⇒
          logger.info(s"access token is $token")
          helpToSaveApiConnector.checkEligibility(nino, token).map {
            case Right(response) => Ok(Json.toJson(response))
            case Left(error) =>
              logger.warn(s"Could not check eligibility due to: $error")
              InternalServerError

          }
        case Left(error) ⇒
          logger.warn(s"Could not get token: $error")
          Future.successful(InternalServerError)
      }.flatMap(identity)
  }
}
