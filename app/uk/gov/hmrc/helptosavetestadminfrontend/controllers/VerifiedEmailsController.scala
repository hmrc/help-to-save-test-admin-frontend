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

package uk.gov.hmrc.helptosavetestadminfrontend.controllers

import com.google.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.helptosavetestadminfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.helptosavetestadminfrontend.forms.EmailsForm
import uk.gov.hmrc.helptosavetestadminfrontend.repos.VerifiedEmailMongoRepository
import uk.gov.hmrc.helptosavetestadminfrontend.views.html._

import scala.concurrent.{ExecutionContext, Future}

class VerifiedEmailsController @Inject()(
  verifiedEmailRepo: VerifiedEmailMongoRepository,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  specify_emails_to_delete: specify_emails_to_delete,
  emails_deleted: emails_deleted
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends AdminFrontendController(mcc, errorHandler) with I18nSupport {

  def deleteVerifiedEmails: Action[AnyContent] = Action.async { implicit request =>
    EmailsForm.deleteEmailsForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(Ok(specify_emails_to_delete(formWithErrors))), { emails =>
          val emailsList: List[String] =
            emails.emails.split(",").toList.map(_.trim)
          verifiedEmailRepo.deleteEmails(emailsList).map {
            case Right(()) => Ok(emails_deleted())
            case Left(errors) =>
              InternalServerError(s"An error occurred, error messages: $errors")
          }
        }
      )
  }

  val specifyEmailsToDelete: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(specify_emails_to_delete(EmailsForm.deleteEmailsForm)))
  }
}
