/*
 * Copyright 2019 HM Revenue & Customs
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


import java.util.UUID
import java.util.concurrent.TimeUnit

import com.google.common.cache._
import com.google.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.connectors.{AuthConnector, OAuthConnector}
import uk.gov.hmrc.helptosavetestadminfrontend.controllers.HelpToSaveApiController.TokenRequest.{PrivilegedTokenRequest, UserRestrictedTokenRequest}
import uk.gov.hmrc.helptosavetestadminfrontend.controllers.HelpToSaveApiController._
import uk.gov.hmrc.helptosavetestadminfrontend.forms.{CreateAccountForm, EligibilityRequestForm, GetAccountForm}
import uk.gov.hmrc.helptosavetestadminfrontend.models._
import uk.gov.hmrc.helptosavetestadminfrontend.util._
import uk.gov.hmrc.helptosavetestadminfrontend.views
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.totp.TotpGenerator

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HelpToSaveApiController @Inject()(authConnector: AuthConnector,
                                        oauthConnector: OAuthConnector,
                                        mcc: MessagesControllerComponents)
                                       (implicit val appConfig: AppConfig, val messageApi: MessagesApi, ec: ExecutionContext)
  extends AdminFrontendController(appConfig, mcc) with I18nSupport with Logging {

  val userIdCache: Cache[UUID, String] =
    CacheBuilder
      .newBuilder
      .maximumSize(100)
      .expireAfterWrite(2, TimeUnit.MINUTES)
      .removalListener(new RemovalListener[UUID, String] {
        override def onRemoval(notification: RemovalNotification[UUID, String]): Unit = {
          logger.info(s"cache entry: (${notification.getKey}) has been removed due to ${notification.getCause}")
        }
      }).build()

  def getCurlRequestIsPage(id: UUID): Action[AnyContent] = Action { implicit request ⇒
    Option(userIdCache.getIfPresent(id)).fold{
      logger.warn(s"Could not find curl request for id: $id")
      internalServerError()
    }{ curl ⇒ Ok(views.html.curl_result(curl)) }
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
        def curlRequest(token: String) =
          s"""
             |curl -v -X GET \\
             |${toCurlRequestLines(params.httpHeaders)}
             |-H "Authorization: Bearer $token" \\
             | "${appConfig.apiUrl}/eligibility${params.requestNino.map("/" + _).getOrElse("")}"
             |""".stripMargin

        val tokenResult = getToken(tokenRequest(params.accessType, AuthUserDetails.empty().copy(nino = params.authNino)))
        handleTokenResult(tokenResult, newId())(curlRequest)
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
          def curlRequest(token: String) = {
            val json = Json.toJson(CreateAccountRequest(params.requestHeaders, params.requestBody))
            s"""
               |curl -v \\
               |${toCurlRequestLines(params.httpHeaders)}
               |-H "Authorization: Bearer $token" \\
               |-d '${json.toString}' "${appConfig.apiUrl}/account"
               |""".stripMargin
          }

          val tokenResult = getToken(tokenRequest(params.accessType, params.authUserDetails))
          handleTokenResult(tokenResult, newId())(curlRequest)
      }
    )
  }

  def oAuthCallback(code: String, id: UUID): Action[AnyContent] = Action.async { implicit request ⇒
    logger.info("handling oAuthCallback from oauth")

    val cookies = request.headers.toMap.get("Cookie").flatMap(_.headOption).getOrElse(throw new RuntimeException("no Cookie found in the headers"))

    oauthConnector.getAccessTokenUserRestricted(code, Some(id), Map("Cookie" -> cookies)).map {
      case Right(AccessToken(token)) ⇒
        val curl =
          Option(userIdCache.getIfPresent(id))
            .getOrElse(throw new RuntimeException("no userId found in the urlMap"))
            .replace("REPLACE", token)

        userIdCache.put(id, curl)
        SeeOther(routes.HelpToSaveApiController.getCurlRequestIsPage(id).url)

      case Left(error) ⇒
        logger.warn(s"Could not get access_token for code ($code) -  $error")
        internalServerError()
    }
  }

  def oAuthCallbackForITests(code: String): Action[AnyContent] = Action.async { implicit request ⇒
    logger.info("handling oAuthCallbackForITests from oauth")

    val cookies = request.headers.toMap.get("Cookie").flatMap(_.headOption).getOrElse(throw new RuntimeException("no Cookie found in the headers"))

    oauthConnector.getAccessTokenUserRestricted(code, None, Map("Cookie" -> cookies)).map {
      case Right(AccessToken(token)) ⇒
        Ok(token)

      case Left(error) ⇒
        logger.warn(s"Could not get access_token for code ($code) -  $error")
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

        def curlRequest(token: String) =
          s"""
             |curl -v -X GET \\
             |${toCurlRequestLines(params.httpHeaders)}
             |-H "Authorization: Bearer $token" \\
             | "${appConfig.apiUrl}/account"
             |""".stripMargin

        val tokenResult = getToken(tokenRequest(params.accessType, AuthUserDetails.empty().copy(nino = params.authNino)))
        handleTokenResult(tokenResult, newId())(curlRequest)
      }
    )
  }

  private def getToken(tokenRequest: TokenRequest): Future[Either[String, Token]] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    tokenRequest match {
      case UserRestrictedTokenRequest(authUserDetails) ⇒
        authConnector.login(authUserDetails)

      case PrivilegedTokenRequest() ⇒
        if(appConfig.runLocal){
          authConnector.getPrivilegedToken()
        } else {
          val totpCode = TotpGenerator.getTotpCode(appConfig.privilegedAccessTOTPSecret)
          oauthConnector.getAccessTokenPrivileged(totpCode, Map.empty)
        }
    }
  }

  private def handleTokenResult(tokenResult: Future[Either[String, Token]], id: UUID)(curl: String => String)(implicit request: Request[_]) = {
    tokenResult.map {
      case Right(AccessToken(token)) ⇒
        if(appConfig.runLocal) {
          sys.error("Generated privileged access token whilst running locally")
        } else {
          userIdCache.put(id, curl(token.stripPrefix("Bearer ")))
          SeeOther(routes.HelpToSaveApiController.getCurlRequestIsPage(id).url)
        }


      case Right(LocalPrivilegedToken(token)) ⇒
        if(appConfig.runLocal){
            userIdCache.put(id, curl(token.stripPrefix("Bearer ")))
            SeeOther(routes.HelpToSaveApiController.getCurlRequestIsPage(id).url)
          } else {
          sys.error("Generated local privileged token but not running locally")
        }

      case Right(SessionToken(session)) ⇒
        if(appConfig.runLocal){
          session.get(SessionKeys.authToken).map(_.stripPrefix("Bearer ")).fold(
            sys.error("Could not find auth token")
          ){ t ⇒
            userIdCache.put(id, curl(t))
            SeeOther(routes.HelpToSaveApiController.getCurlRequestIsPage(id).url)
          }
        } else {
          userIdCache.put(id, curl("REPLACE"))
          SeeOther(appConfig.authorizeUrl(id)).withSession(session)
        }

      case Left(e) ⇒
        logger.warn(s"error getting the access token, error=$e")
        internalServerError()
    }.recover {
      case e ⇒ logger.warn(s"error getting the access token, error=$e")
        internalServerError()
    }
  }

  private def tokenRequest(accessType: AccessType, authUserDetails: AuthUserDetails): TokenRequest = accessType match {
    case Privileged ⇒ PrivilegedTokenRequest()
    case UserRestricted ⇒ UserRestrictedTokenRequest(authUserDetails)
  }

  private def toCurlRequestLines(httpHeaders: HttpHeaders): String =
    httpHeaders.toMap().map { case (k, v) ⇒ s"""-H "$k: $v" \\""" }.mkString("\n")

  private def newId(): UUID = UUID.randomUUID()

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