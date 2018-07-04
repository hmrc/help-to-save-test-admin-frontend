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

package uk.gov.hmrc.helptosavetestadminfrontend.forms

import play.api.data.Forms._
import play.api.data._
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessType
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessFormatter._

object CreateAccountForm {

  val httpHeaderMapping = mapping(
    "contentType" -> nonEmptyText,
    "accept" -> nonEmptyText,
    "govClientUserId" -> nonEmptyText,
    "govClientTimezone" -> nonEmptyText,
    "govVendorVersion" -> nonEmptyText,
    "govVendorInstanceId" -> nonEmptyText
  )(HttpHeaders.apply)(HttpHeaders.unapply)

  val requestHeaderMapping = mapping(
    "version" -> nonEmptyText,
    "createdTimestamp" -> nonEmptyText,
    "clientCode" -> nonEmptyText,
    "requestCorrelationId" -> nonEmptyText
  )(RequestHeaders.apply)(RequestHeaders.unapply)

  val requestBodyMapping = mapping(
    "authNino" -> optional(text),
    "requestNino" -> optional(text),
    "forename" -> nonEmptyText,
    "surname" -> nonEmptyText,
    "dateOfBirth" -> nonEmptyText,
    "contactDetails" -> mapping(
      "address1" -> nonEmptyText,
      "address2" -> nonEmptyText,
      "address3" -> optional(text),
      "address4" -> optional(text),
      "address5" -> optional(text),
      "postcode" -> nonEmptyText,
      "countryCode" -> optional(text),
      "communicationPreference" -> nonEmptyText,
      "email" -> optional(text),
      "phoneNumber" -> optional(text)
    )(ContactDetails.apply)(ContactDetails.unapply),
    "registrationChannel" -> nonEmptyText,
    "accessType" -> of(accessFormatter)
  )(RequestBody.apply)(RequestBody.unapply)
  
  def createAccountForm = Form(
    mapping(
      "httpHeaders" -> httpHeaderMapping,
      "requestHeaders" -> requestHeaderMapping,
      "requestBody" -> requestBodyMapping
    )(CreateAccountParams.apply)(CreateAccountParams.unapply)
  )

}

case class CreateAccountParams(httpHeaders: HttpHeaders,
                               requestHeaders: RequestHeaders,
                               requestBody: RequestBody
                              )

case class ContactDetails(address1: String,
                          address2: String,
                          address3: Option[String],
                          address4: Option[String],
                          address5: Option[String],
                          postcode: String,
                          countryCode: Option[String],
                          communicationPreference: String,
                          email: Option[String],
                          phoneNumber: Option[String])

case class HttpHeaders(contentType: String,
                       accept: String,
                       govClientUserId: String,
                       govClientTimezone: String,
                       govVendorVersion: String,
                       govVendorInstanceId: String
                      )

case class RequestHeaders(version: String,
                          createdTimestamp: String,
                          clientCode: String,
                          requestCorrelationId: String
                         )

case class RequestBody(authNino: Option[String],
                       requestNino: Option[String],
                       forename: String,
                       surname: String,
                       dateOfBirth: String,
                       contactDetails: ContactDetails,
                       registrationChannel: String,
                       accessType: AccessType
                      )
