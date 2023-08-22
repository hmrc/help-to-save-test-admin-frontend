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

package uk.gov.hmrc.helptosavetestadminfrontend.repos

import com.google.inject.{Inject, Singleton}
import org.mongodb.scala.bson.Document
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.model.Indexes.ascending
import uk.gov.hmrc.helptosavetestadminfrontend.forms.Email
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifiedEmailMongoRepository @Inject()(mongo: MongoComponent)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[Email](
      collectionName = "verifiedEmail",
      mongoComponent = mongo,
      domainFormat = Email.emailFormats,
      indexes = Seq(
        IndexModel(ascending("_id"))
      )
    ) {

  def deleteEmails(emails: List[String]): Future[Either[List[String], Unit]] = {

    val result: List[Future[Either[String, Unit]]] = emails.map { email =>
      collection
        .deleteOne(Document("email" -> email))
        .toFuture()
        .map { res =>
          Right(())
        }
        .recover {
          case e => Left(s"${e.getMessage}")
        }
    }

    Future.sequence(result).map { x =>
      val errors = x.collect { case Left(s) => s }
      if (errors.nonEmpty) {
        Left(errors)
      } else {
        Right(())
      }
    }
  }

}
