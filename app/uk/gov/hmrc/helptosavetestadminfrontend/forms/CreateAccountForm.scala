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

object CreateAccountForm {

  def createAccountForm = Form(
    mapping(
      "contentType" -> nonEmptyText,
      "accept" -> nonEmptyText,
      "govClientUserId" -> nonEmptyText,
      "govClientTimezone" -> nonEmptyText,
      "govVendorVersion" -> nonEmptyText,
      "govVendorInstanceId" -> nonEmptyText,
      "cacheControl" -> nonEmptyText,
      "version" -> nonEmptyText,
      "createdTimestamp" -> nonEmptyText,
      "clientCode" -> nonEmptyText,
      "requestCorrelationId" -> nonEmptyText,
      "nino" -> nonEmptyText,
      "forename" -> nonEmptyText,
      "surname" -> nonEmptyText,
      "dateOfBirth" -> nonEmptyText,
      "address1" -> nonEmptyText,
      "address2" -> nonEmptyText,
      "postcode" -> nonEmptyText,
      "countryCode" -> nonEmptyText,
      "communicationPreference" -> nonEmptyText,
      "registrationChannel" -> nonEmptyText
    )(CreateAccountParams.apply)(CreateAccountParams.unapply)
  )

}

case class CreateAccountParams(contentType: String,
                               accept: String,
                               govClientUserId: String,
                               govClientTimezone: String,
                               govVendorVersion: String,
                               govVendorInstanceId: String,
                               cacheControl: String,
                               version: String,
                               createdTimestamp: String,
                               clientCode: String,
                               requestCorrelationId: String,
                               nino: String,
                               forename: String,
                               surname: String,
                               dateOfBirth: String,
                               address1: String,
                               address2: String,
                               postcode: String,
                               countryCode: String,
                               communicationPreference: String,
                               registrationChannel: String
                              )
