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

object GetAccountForm {

  def getAccountForm = Form(
    mapping(
      "accept" -> nonEmptyText,
      "govClientUserId" -> nonEmptyText,
      "govClientTimezone" -> nonEmptyText,
      "govVendorVersion" -> nonEmptyText,
      "govVendorInstanceId" -> nonEmptyText,
      "authNino" -> optional(text)
    )(GetAccountParams.apply)(GetAccountParams.unapply)
  )

}

case class GetAccountParams(accept: String,
                             govClientUserId: String,
                             govClientTimezone: String,
                             govVendorVersion: String,
                             govVendorInstanceId: String,
                             authNino: Option[String]
                            )