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

import com.google.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.helptosavetestadminfrontend.views

@Singleton
class DummyPagesController @Inject() (implicit val messagesApi: MessagesApi, appConfig: AppConfig) extends FrontendController with I18nSupport {

  def accountHomepage: Action[AnyContent] = Action { implicit request ⇒
    Ok(views.html.account_homepage())
  }


  def payIn: Action[AnyContent] = Action { implicit request ⇒
    Ok(views.html.pay_in())
  }

}