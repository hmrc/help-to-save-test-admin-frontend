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
import uk.gov.hmrc.helptosavetestadminfrontend.models.HttpHeaders
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessFormatter.accessFormatter
import uk.gov.hmrc.helptosavetestadminfrontend.util.AccessType

object GetAccountForm {
  def getAccountForm: Form[GetAccountParams] = Form(
    mapping(
      "httpHeaders" -> mapping(
        "accept"              -> optional(text),
        "govClientUserId"     -> optional(text),
        "govClientTimezone"   -> optional(text),
        "govVendorVersion"    -> optional(text),
        "govVendorInstanceId" -> optional(text)
      ) { case (a, clientId, clientTimeZone, vendorVersion, vendorId) =>
        HttpHeaders(a, None, clientId, clientTimeZone, vendorVersion, vendorId)
      } { h =>
        Some((h.accept, h.govClientUserId, h.govClientTimezone, h.govVendorVersion, h.govVendorInstanceId))
      },
      "authNino"   -> optional(text),
      "accessType" -> of(accessFormatter)
    )(GetAccountParams.apply)(o => Some((o.httpHeaders, o.authNino, o.accessType)))
  )
}

case class GetAccountParams(httpHeaders: HttpHeaders, authNino: Option[String], accessType: AccessType)
