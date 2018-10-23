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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.helptosavetestadminfrontend.models.CreateAccountRequest.CreateAccountBody.{BankDetails, ContactDetails}
import uk.gov.hmrc.helptosavetestadminfrontend.models.CreateAccountRequest.{CreateAccountBody, CreateAccountHeader}

case class CreateAccountRequest(header: CreateAccountHeader, body: CreateAccountBody)

object CreateAccountRequest {

  case class CreateAccountHeader(version: Option[String],
                                 createdTimestamp: Option[String],
                                 clientCode: Option[String],
                                 requestCorrelationId: Option[String])

  case class CreateAccountBody(nino: Option[String],
                               forename: Option[String],
                               surname: Option[String],
                               dateOfBirth: Option[String],
                               contactDetails: ContactDetails,
                               registrationChannel: Option[String],
                               bankDetails: BankDetails)

  object CreateAccountBody {

    case class ContactDetails(address1: Option[String],
                              address2: Option[String],
                              address3: Option[String],
                              address4: Option[String],
                              address5: Option[String],
                              postcode: Option[String],
                              countryCode: Option[String],
                              communicationPreference: Option[String],
                              email: Option[String],
                              phoneNumber: Option[String])

    case class BankDetails(sortCode: Option[String],
                           accountNumber: Option[String],
                           rollNumber: Option[String],
                           accountName: Option[String])

  }

  implicit val headerFormat: Format[CreateAccountHeader] = Json.format[CreateAccountHeader]
  implicit val contactDetailsFormat: Format[ContactDetails] = Json.format[ContactDetails]
  implicit val bankDetailsFormat: Format[BankDetails] = Json.format[BankDetails]
  implicit val bodyFormat: Format[CreateAccountBody] = Json.format[CreateAccountBody]
  implicit val requestFormat: Format[CreateAccountRequest] = Json.format[CreateAccountRequest]

}
