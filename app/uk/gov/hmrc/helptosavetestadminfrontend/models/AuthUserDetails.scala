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

case class AuthUserDetails(
                            nino: Option[String],
                            forename:Option[String],
                            surname: Option[String],
                            dateOfBirth: Option[String],
                            address1: Option[String],
                            address2: Option[String],
                            address3: Option[String],
                            address4: Option[String],
                            address5: Option[String],
                            postcode: Option[String],
                            countryCode: Option[String],
                            email: Option[String]
                          )

object AuthUserDetails {

  def empty(): AuthUserDetails = AuthUserDetails(None, None, None, None, None, None, None,None, None, None, None, None)

}