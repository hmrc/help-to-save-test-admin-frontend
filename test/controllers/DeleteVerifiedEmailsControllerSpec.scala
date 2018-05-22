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

package controllers

import play.api.mvc.Result
import uk.gov.hmrc.helptosavetestadminfrontend.controllers.DeleteVerifiedEmailsController
import uk.gov.hmrc.helptosavetestadminfrontend.repos.VerifiedEmailMongoRepository

import scala.concurrent.{ExecutionContext, Future}

class DeleteVerifiedEmailsControllerSpec extends TestSupport with CSRFSupport {

  val store = mock[VerifiedEmailMongoRepository]

  val controller = new DeleteVerifiedEmailsController(store)(appConfig, messagesApi)

  def mockDeleteEmails(emails: List[String])(result: Future[Either[List[String], Unit]]): Unit =
    (store.deleteEmails(_: List[String])(_: ExecutionContext))
    .expects(emails, *)
    .returning(result)

  "handling delete email form submits" must {

    val email1: String = "email1@gmail.com"
    val email2: String = "email2@gmail.com"
    val emails: List[String] = List(email1, email2)

    def submit(): Future[Result] =
      controller.deleteVerifiedEmails()(fakeRequestWithCSRFToken.withFormUrlEncodedBody("emails" → s"$email1, $email2"))

    "return 200 status and the emails deleted page when call to delete emails is successful" in {
      mockDeleteEmails(emails)(Future.successful(Right(())))

      val result = submit()
      status(result) shouldBe 200
    }

    "return an Internal Server Error (500) status when call to delete emails is unsuccessful" in {
      mockDeleteEmails(emails)(Future.successful(Left(List("An error occurred, error messages: "))))

      val result = submit()
      status(result) shouldBe 500
    }
  }

}
