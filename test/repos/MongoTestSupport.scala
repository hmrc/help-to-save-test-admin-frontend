/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.{UpdateWriteResult, WriteError, WriteResult}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.{MongoConnector, ReactiveRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MongoTestSupport[Data, Repo <: ReactiveRepository[Data, BSONObjectID]] {
  this: MockFactory ⇒

  trait MockDBFunctions {
    def remove(query: (String, Json.JsValueWrapper)*): Future[WriteResult]
  }

  val mockDBFunctions = mock[MockDBFunctions]

  val mockMongo = mock[ReactiveMongoComponent]

  def newMongoStore(): Repo

  lazy val mongoStore: Repo = {
    val connector = mock[MongoConnector]
    (mockMongo.mongoConnector _).expects().returning(connector)
    // we are using null as the constructor of DefaultDB is private
    (connector.db _).expects().returning(() ⇒ null)
    newMongoStore()
  }

  def mockRemove(query: (String, Json.JsValueWrapper)*)(result: ⇒ Future[Either[String, Unit]]): Unit =
    (mockDBFunctions.remove _)
    .expects(query)
    .returning(result.map{
      case Left(error) ⇒ UpdateWriteResult(false, 1, 1, Seq.empty, Seq(WriteError(1, 400, error)), None, None, None)
      case Right(()) ⇒ UpdateWriteResult(true, 1, 1, Seq.empty, Seq.empty, None, None, None)
    })
}
