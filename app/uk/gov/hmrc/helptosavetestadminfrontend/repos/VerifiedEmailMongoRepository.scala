/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.helptosavetestadminfrontend.repos


import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.helptosavetestadminfrontend.forms.Email
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[VerifiedEmailMongoRepositoryImpl])
trait VerifiedEmailMongoRepository {

  def deleteEmails(emails: List[String])(implicit ec: ExecutionContext): Future[Either[List[String], Unit]]

}

@Singleton
class VerifiedEmailMongoRepositoryImpl @Inject()(mongo:   ReactiveMongoComponent)
  extends ReactiveRepository[Email, BSONObjectID](
    collectionName = "verifiedEmail",
    mongo = mongo.mongoConnector.db,
    domainFormat = Email.emailFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats
  ) with VerifiedEmailMongoRepository {


  def deleteEmails(emails: List[String])(implicit ec: ExecutionContext): Future[Either[List[String], Unit]] = {

    val result: List[Future[Either[String, Unit]]] =  emails.map { email ⇒
      remove("email" -> email).map { res ⇒
        if (res.writeErrors.nonEmpty) {
          Left(s"An error has occurred while deleting email: $email, errors: ${res.writeErrors.map(_.errmsg).mkString(",")}")
        } else {
          Right(())
        }
      }
    }

    Future.sequence(result).map{ x ⇒
      val errors = x.collect{ case Left(s) ⇒ s }
      if(errors.nonEmpty){
        Left(errors)
      } else {
        Right(())
      }
    }
  }

}

