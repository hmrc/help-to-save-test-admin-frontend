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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging

import scala.concurrent.Future

class HelpToSaveApiController @Inject()(http: WSHttp)(implicit override val appConfig: AppConfig, val messageApi: MessagesApi)
  extends AdminFrontendController(messageApi, appConfig) with I18nSupport with Logging {

  def checkEligibility: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(SeeOther(appConfig.oAuthRedirectUrl(appConfig.eligibilityCallbackUrl)))
  }

  def eligibilityCallback: Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"inside eligibilityCallback, queryString =${request.queryString}")
    Future.successful(Ok("success"))
  }

  def accountCallback: Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"inside accountCallback, queryString =${request.queryString}")
    Future.successful(Ok("success"))
  }

}
