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


import com.google.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.{AuthConnector, OAuthConnector}
import uk.gov.hmrc.helptosavetestadminfrontend.controllers.HelpToSaveApiController.TokenRequest.{PrivilegedTokenRequest, UserRestrictedTokenRequest}
import uk.gov.hmrc.helptosavetestadminfrontend.controllers.HelpToSaveApiController._
import uk.gov.hmrc.helptosavetestadminfrontend.forms.{CreateAccountForm, EligibilityRequestForm, GetAccountForm}
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.models._
import uk.gov.hmrc.helptosavetestadminfrontend.util._
import uk.gov.hmrc.helptosavetestadminfrontend.views
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.totp.TotpGenerator

import scala.collection.mutable
import scala.concurrent.Future

@Singleton
class HelpToSaveApiController @Inject()(http: WSHttp, authConnector: AuthConnector, oauthConnector: OAuthConnector)
                                       (implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  val urlMap = mutable.Map.empty[String, String]

  private def getToken(tokenRequest: TokenRequest): Future[Either[String, Token]] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    tokenRequest match {
      case UserRestrictedTokenRequest(authUserDetails) ⇒
        authConnector.login(authUserDetails)

      case PrivilegedTokenRequest() ⇒
        val totpCode = TotpGenerator.getTotpCode(appConfig.privilegedAccessTOTPSecret)
        oauthConnector.getAccessToken(totpCode, None, Privileged, Map.empty)
    }
  }

  private def handleTokenResult(tokenResult: Future[Either[String, Token]])(curl: String => String) = {
    tokenResult.map {
      case Right(AccessToken(token)) ⇒
        Ok(curl(token))

      case Right(SessionToken(session)) =>
        val userId = session.get(SessionKeys.userId).getOrElse(throw new RuntimeException("no userId found in the session"))
        urlMap.put(userId, curl("REPLACE"))
        SeeOther(appConfig.authorizeUrl(userId)).withSession(session)

      case Left(e) ⇒
        logger.warn(s"error getting the access token, error=$e")
        internalServerError()
    }.recover {
      case e ⇒ logger.warn(s"error getting the access token, error=$e")
        internalServerError()
    }
  }

  def availableFunctions(): Action[AnyContent] = Action.async { implicit request ⇒
    Future.successful(Ok(views.html.availableFunctions()))
  }

  def getCheckEligibilityPage(): Action[AnyContent] = Action.async { implicit request ⇒
    Future.successful(Ok(views.html.get_check_eligibility_page(EligibilityRequestForm.eligibilityForm)))
  }

  def checkEligibility(): Action[AnyContent] = Action.async { implicit request ⇒
    EligibilityRequestForm.eligibilityForm.bindFromRequest().fold(
      formWithErrors ⇒ Future.successful(Ok(views.html.get_check_eligibility_page(formWithErrors))),
      { params ⇒
        def url(token: String) =
          s"""
             |curl -v -X GET \\
             |${toCurlRequestLines(params.httpHeaders)}
             |-H "Authorization: Bearer $token" \\
             | "${appConfig.apiUrl}/eligibility${params.requestNino.map("/" + _).getOrElse("")}"
             |""".stripMargin

        val tokenResult = getToken(tokenRequest(params.accessType, AuthUserDetails.empty().copy(nino = params.authNino)))
        handleTokenResult(tokenResult)(url)
      }
    )
  }

  def getCreateAccountPage(): Action[AnyContent] = Action.async { implicit request ⇒
    Future.successful(Ok(views.html.get_create_account_page(CreateAccountForm.createAccountForm)))
  }

  def createAccount(): Action[AnyContent] = Action.async { implicit request ⇒
    CreateAccountForm.createAccountForm.bindFromRequest().fold(
      formWithErrors ⇒ Future.successful(Ok(views.html.get_create_account_page(formWithErrors))),
      {
        params ⇒
          def url(token: String) = {
            val json = Json.toJson(CreateAccountRequest(params.requestHeaders, params.requestBody))
            s"""
               |curl -v \\
               |${toCurlRequestLines(params.httpHeaders)}
               |-H "Authorization: Bearer $token" \\
               |-d '${json.toString}' "${appConfig.apiUrl}/account"
               |""".stripMargin
          }

          val tokenResult = getToken(tokenRequest(params.accessType, params.authUserDetails))
          handleTokenResult(tokenResult)(url)
      }
    )
  }

  def oAuthCallback(code: String, userId: Option[String]): Action[AnyContent] = Action.async { implicit request ⇒
    logger.info("handling authLoginStubCallback from oauth")

    val cookies = request.headers.toMap.get("Cookie").flatMap(_.headOption).getOrElse(throw new RuntimeException("no Cookie found in the headers"))

    logger.info(s"cookies = $cookies")

    oauthConnector.getAccessToken(code, userId, UserRestricted, Map("Cookie" -> cookies)).map {
      case Right(AccessToken(token)) ⇒
        val userIdKey = userId.getOrElse(throw new RuntimeException("no userId found in the oAuthCallback request"))
        Ok(urlMap.getOrElse(userIdKey, throw new RuntimeException("no userId found in the urlMap")).replace("REPLACE", token))

      case Left(error) ⇒
        logger.warn(s"Could not get token: $error")
        internalServerError()
    }
  }

  def getAccountPage(): Action[AnyContent] = Action.async { implicit request ⇒
    Future.successful(Ok(views.html.get_account_page(GetAccountForm.getAccountForm)))
  }

  def getAccount(): Action[AnyContent] = Action.async { implicit request ⇒
    GetAccountForm.getAccountForm.bindFromRequest().fold(
      formWithErrors ⇒ Future.successful(Ok(views.html.get_account_page(formWithErrors))),
      { params ⇒

        def url(token: String) =
          s"""
             |curl -v -X GET \\
             |${toCurlRequestLines(params.httpHeaders)}
             |-H "Authorization: Bearer $token" \\
             | "${appConfig.apiUrl}/account"
             |""".stripMargin

        val tokenResult = getToken(tokenRequest(params.accessType, AuthUserDetails.empty().copy(nino = params.authNino)))
        handleTokenResult(tokenResult)(url)
      }
    )
  }

  private def tokenRequest(accessType: AccessType, authUserDetails: AuthUserDetails): TokenRequest = accessType match {
    case Privileged ⇒ PrivilegedTokenRequest()
    case UserRestricted ⇒ UserRestrictedTokenRequest(authUserDetails)
  }

  private def toCurlRequestLines(httpHeaders: HttpHeaders): String =
    httpHeaders.toMap().map { case (k, v) ⇒ s"""-H "$k: $v" \\""" }.mkString("\n")


}

object HelpToSaveApiController {

  sealed trait TokenRequest

  object TokenRequest {

    case class UserRestrictedTokenRequest(authUserDetails: AuthUserDetails) extends TokenRequest

    case class PrivilegedTokenRequest() extends TokenRequest

  }

  implicit class HttpHeadersOps(val h: HttpHeaders) extends AnyVal {

    def toMap(): Map[String, String] =
      List(
        "Accept" → h.accept,
        "Content-Type" → h.contentType,
        "Gov-Client-User-ID" → h.govClientUserId,
        "Gov-Client-Timezone" → h.govClientTimezone,
        "Gov-Vendor-Version" → h.govVendorVersion,
        "Gov-Vendor-Instance-ID" → h.govVendorInstanceId
      ).collect { case (k, Some(v)) ⇒ k → v }.toMap

  }

}