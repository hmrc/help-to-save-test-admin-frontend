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

package repos

import controllers.TestSupport
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.helptosavetestadminfrontend.forms.Email
import uk.gov.hmrc.helptosavetestadminfrontend.repos.VerifiedEmailMongoRepositoryImpl

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class VerifiedEmailMongoRepositorySpec extends TestSupport with MongoTestSupport[Email, VerifiedEmailMongoRepositoryImpl] {

  def newMongoStore() = new VerifiedEmailMongoRepositoryImpl(mockMongo) {

    override def remove(query: (String, Json.JsValueWrapper)*)(implicit ec: ExecutionContext): Future[WriteResult] =
      mockDBFunctions.remove(query: _*)
  }

  def mockDeleteEmail(email: String)(result: Future[Either[String, Unit]]): Unit =
    mockRemove("email" → email)(result)

  "deleteEmails" must {

    def delete(emails: List[String]): Either[List[String], Unit] =
      Await.result(mongoStore.deleteEmails(emails), 5.seconds)

    val email1: String = "email1@gmail.com"
    val email2: String = "email2@gmail.com"
    val emails: List[String] = List(email1, email2)

    "return a Right when all emails are successfully deleted" in {
      inSequence {
        mockDeleteEmail(email1)(Future.successful(Right(())))
        mockDeleteEmail(email2)(Future.successful(Right(())))
      }

      delete(emails) shouldBe Right(())
    }

    "return a Left when an error occurs when deleting an email" in {
      inSequence {
        mockDeleteEmail(email1)(Future.successful(Right(())))
        mockDeleteEmail(email2)(Future.successful(Left(s"An error has occurred while deleting email: $email2")))
      }

      delete(emails) shouldBe Left(List(s"An error has occurred while deleting email: $email2, error: None"))
    }
  }

}
