/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.helptosavetestadminfrontend.models.{AuthUserDetails, HttpHeaders}
import uk.gov.hmrc.helptosavetestadminfrontend.models.CreateAccountRequest.CreateAccountBody.{BankDetails, ContactDetails}
import uk.gov.hmrc.helptosavetestadminfrontend.models.CreateAccountRequest.{CreateAccountBody, CreateAccountHeader}
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessType
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessFormatter._

object CreateAccountForm {

  val httpHeaderMapping = mapping(
    "accept" -> optional(text),
    "contentType" -> optional(text),
    "govClientUserId" -> optional(text),
    "govClientTimezone" -> optional(text),
    "govVendorVersion" -> optional(text),
    "govVendorInstanceId" -> optional(text)
  )(HttpHeaders.apply)(HttpHeaders.unapply)

  val requestHeaderMapping = mapping(
    "version" -> optional(text),
    "createdTimestamp" -> optional(text),
    "clientCode" -> optional(text),
    "requestCorrelationId" -> optional(text)
  )(CreateAccountHeader.apply)(CreateAccountHeader.unapply)

  val requestBodyMapping = mapping(
    "nino" -> optional(text),
    "forename" -> optional(text),
    "surname" -> optional(text),
    "dateOfBirth" -> optional(text),
    "contactDetails" -> mapping(
      "address1" -> optional(text),
      "address2" -> optional(text),
      "address3" -> optional(text),
      "address4" -> optional(text),
      "address5" -> optional(text),
      "postcode" -> optional(text),
      "countryCode" -> optional(text),
      "communicationPreference" -> optional(text),
      "email" -> optional(text),
      "phoneNumber" -> optional(text)
    )(ContactDetails.apply)(ContactDetails.unapply),
    "registrationChannel" -> optional(text),
    "bankDetails" → mapping(
      "sortCode" → optional(text),
      "accountNumber" → optional(text),
      "rollNumber" → optional(text),
      "accountName" → optional(text)
    )(BankDetails.apply)(BankDetails.unapply)
  )(CreateAccountBody.apply)(CreateAccountBody.unapply)


  val authUserDetailsMapping = mapping(
    "nino" -> optional(text),
    "forename" -> optional(text),
    "surname" -> optional(text),
    "dateOfBirth" -> optional(text),
    "address1" -> optional(text),
    "address2" -> optional(text),
    "address3" -> optional(text),
    "address4" -> optional(text),
    "address5" -> optional(text),
    "postcode" -> optional(text),
    "countryCode" -> optional(text),
    "email" -> optional(text)
  )(AuthUserDetails.apply)(AuthUserDetails.unapply)


  def createAccountForm = Form(
    mapping(
      "httpHeaders" → httpHeaderMapping,
      "requestHeaders" → requestHeaderMapping,
      "requestBody" → requestBodyMapping,
      "authUserDetails" → authUserDetailsMapping,
      "accessType" -> of(accessFormatter)
    )(CreateAccountParams.apply)(CreateAccountParams.unapply)
  )

}

case class CreateAccountParams(httpHeaders: HttpHeaders,
                               requestHeaders: CreateAccountHeader,
                               requestBody: CreateAccountBody,
                               authUserDetails: AuthUserDetails,
                               accessType: AccessType
                              )



