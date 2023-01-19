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

package repos

import controllers.UnitSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.helptosavetestadminfrontend.forms.Email
import uk.gov.hmrc.helptosavetestadminfrontend.repos.VerifiedEmailMongoRepository
import uk.gov.hmrc.mongo.test.{DefaultPlayMongoRepositorySupport, MongoSupport}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class VerifiedEmailMongoRepositorySpec extends MongoSupport with MockitoSugar with UnitSpec with DefaultPlayMongoRepositorySupport[Email] {
  override lazy val repository = new VerifiedEmailMongoRepository(mongoComponent)

  "deleteEmails" must {
    def delete(emails: List[String]): Either[List[String], Unit] =
      Await.result(repository.deleteEmails(emails), 5.seconds)

    val email1: Email = Email("email1@gmail.com")
    val email2: Email = Email("email2@gmail.com")
    val emails: List[String] = List(email1.emails, email2.emails)

    "return a Right when all emails are successfully deleted" in {
      dropDatabase()
      repository.collection.insertMany(Seq(email1,email2))
      delete(emails) shouldBe Right(())
    }
  }

}
