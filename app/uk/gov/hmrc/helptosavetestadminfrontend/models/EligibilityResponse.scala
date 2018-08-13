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

package uk.gov.hmrc.helptosavetestadminfrontend.models

import cats.Eq
import cats.instances.boolean._
import cats.instances.int._
import cats.instances.string._
import cats.syntax.eq._
import play.api.libs.json.{JsValue, _}

import scala.collection.Map


sealed trait EligibilityResponse

object EligibilityResponse {

  implicit val apiEligibilityResponseWrites: Writes[ApiEligibilityResponse] = Json.writes[ApiEligibilityResponse]

  val accountExistsJSON: JsValue = Json.parse("""{"accountExists": true}""")

  implicit val writes: Writes[EligibilityResponse] = new Writes[EligibilityResponse] {
    override def writes(response: EligibilityResponse): JsValue = {
      response match {
        case a: ApiEligibilityResponse ⇒
          Json.toJson(a)
        case _: AccountAlreadyExists ⇒
          accountExistsJSON
      }
    }
  }

  implicit val jsValueEq: Eq[JsValue] = new Eq[JsValue] {
    override def eqv(x: JsValue, y: JsValue): Boolean = (x, y) match {
      case (JsString(s1), JsString(s2)) ⇒
        s1 === s2

      case (JsBoolean(b1), JsBoolean(b2)) ⇒
        b1 === b2

      case (JsNumber(n1), JsNumber(n2)) ⇒
        n1.equals(n2)

      case (JsArray(a1), JsArray(a2)) ⇒
        (a1.size === a2.size) && a1.zip(a2).forall { case (j1, j2) ⇒ eqv(j1, j2) }

      case (JsObject(m1), JsObject(m2)) ⇒
        def compare(m1: Map[String, JsValue], m2: Map[String, JsValue]) =
          m1.forall { case (k1, v1) ⇒ m2.get(k1).exists(v2 ⇒ eqv(v1, v2)) }

        compare(m1, m2) && compare(m2, m1)

      case (JsNull, JsNull) ⇒
        true

      case _ ⇒
        false

    }
  }

  implicit val reads: Reads[EligibilityResponse] = new Reads[EligibilityResponse] {
    override def reads(json: JsValue): JsResult[EligibilityResponse] =
      if (json === accountExistsJSON) {
        JsSuccess(AccountAlreadyExists())
      } else {
        json.validate(ApiEligibilityResponse.apiEligibilityResponseFormat)
      }
  }

}

case class ApiEligibilityResponse(eligibility: Eligibility, accountExists: Boolean) extends EligibilityResponse

object ApiEligibilityResponse {
  implicit val apiEligibilityResponseFormat: Format[ApiEligibilityResponse] = Json.format[ApiEligibilityResponse]
}

case class AccountAlreadyExists() extends EligibilityResponse

case class Eligibility(isEligible: Boolean, hasWTC: Boolean, hasUC: Boolean)

object Eligibility {
  implicit val eligibilityFormat: Format[Eligibility] = Json.format[Eligibility]

}