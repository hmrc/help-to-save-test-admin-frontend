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

package uk.gov.hmrc.helptosavetestadminfrontend.forms

import play.api.data.Forms._
import play.api.data._
import uk.gov.hmrc.helptosavetestadminfrontend.models.CreateAccountRequest.CreateAccountBody.{BankDetails, ContactDetails}
import uk.gov.hmrc.helptosavetestadminfrontend.models.CreateAccountRequest.{CreateAccountBody, CreateAccountHeader}
import uk.gov.hmrc.helptosavetestadminfrontend.models.{AuthUserDetails, HttpHeaders}
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessFormatter._
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessType

object CreateAccountForm {
  private val httpHeaderMapping: Mapping[HttpHeaders] = mapping(
    "accept"              -> optional(text),
    "contentType"         -> optional(text),
    "govClientUserId"     -> optional(text),
    "govClientTimezone"   -> optional(text),
    "govVendorVersion"    -> optional(text),
    "govVendorInstanceId" -> optional(text)
  )(HttpHeaders.apply)(o =>
    Some((o.accept, o.contentType, o.govClientUserId, o.govClientTimezone, o.govVendorVersion, o.govVendorInstanceId))
  )

  private val requestHeaderMapping: Mapping[CreateAccountHeader] = mapping(
    "version"              -> optional(text),
    "createdTimestamp"     -> optional(text),
    "clientCode"           -> optional(text),
    "requestCorrelationId" -> optional(text)
  )(CreateAccountHeader.apply)(o => Some((o.version, o.createdTimestamp, o.clientCode, o.requestCorrelationId)))

  private val contactDetailsMapping: Mapping[ContactDetails] = mapping(
    "address1"                -> optional(text),
    "address2"                -> optional(text),
    "address3"                -> optional(text),
    "address4"                -> optional(text),
    "address5"                -> optional(text),
    "postcode"                -> optional(text),
    "countryCode"             -> optional(text),
    "communicationPreference" -> optional(text),
    "email"                   -> optional(text),
    "phoneNumber"             -> optional(text)
  )(ContactDetails.apply)(o =>
    Some(
      (
        o.address1,
        o.address2,
        o.address3,
        o.address4,
        o.address5,
        o.postcode,
        o.countryCode,
        o.communicationPreference,
        o.email,
        o.phoneNumber
      )
    )
  )

  private val bankDetailsMapping: Mapping[BankDetails] = mapping(
    "sortCode"      -> optional(text),
    "accountNumber" -> optional(text),
    "rollNumber"    -> optional(text),
    "accountName"   -> optional(text)
  )(BankDetails.apply)(o => Some((o.sortCode, o.accountNumber, o.rollNumber, o.accountName)))

  private val requestBodyMapping: Mapping[CreateAccountBody] = mapping(
    "nino"                -> optional(text),
    "forename"            -> optional(text),
    "surname"             -> optional(text),
    "dateOfBirth"         -> optional(text),
    "contactDetails"      -> contactDetailsMapping,
    "registrationChannel" -> optional(text),
    "bankDetails"         -> bankDetailsMapping
  )(CreateAccountBody.apply)(o =>
    Some((o.nino, o.forename, o.surname, o.dateOfBirth, o.contactDetails, o.registrationChannel, o.bankDetails))
  )

  private val authUserDetailsMapping: Mapping[AuthUserDetails] = mapping(
    "nino"        -> optional(text),
    "forename"    -> optional(text),
    "surname"     -> optional(text),
    "dateOfBirth" -> optional(text),
    "address1"    -> optional(text),
    "address2"    -> optional(text),
    "address3"    -> optional(text),
    "address4"    -> optional(text),
    "address5"    -> optional(text),
    "postcode"    -> optional(text),
    "countryCode" -> optional(text),
    "email"       -> optional(text)
  )(AuthUserDetails.apply)(o =>
    Some(
      (
        o.nino,
        o.forename,
        o.surname,
        o.dateOfBirth,
        o.address1,
        o.address2,
        o.address3,
        o.address4,
        o.address5,
        o.postcode,
        o.countryCode,
        o.email
      )
    )
  )

  def createAccountForm: Form[CreateAccountParams] = Form(
    mapping(
      "httpHeaders"     -> httpHeaderMapping,
      "requestHeaders"  -> requestHeaderMapping,
      "requestBody"     -> requestBodyMapping,
      "authUserDetails" -> authUserDetailsMapping,
      "accessType"      -> of(accessFormatter)
    )(CreateAccountParams.apply)(o =>
      Some((o.httpHeaders, o.requestHeaders, o.requestBody, o.authUserDetails, o.accessType))
    )
  )
}

case class CreateAccountParams(
  httpHeaders: HttpHeaders,
  requestHeaders: CreateAccountHeader,
  requestBody: CreateAccountBody,
  authUserDetails: AuthUserDetails,
  accessType: AccessType
)
