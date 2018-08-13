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

package uk.gov.hmrc.helptosavetestadminfrontend.connectors

import com.google.inject.Inject
import play.mvc.Http.Status
import uk.gov.hmrc.helptosavetestadminfrontend.config.AppConfig
import uk.gov.hmrc.helptosavetestadminfrontend.http.WSHttp
import uk.gov.hmrc.helptosavetestadminfrontend.models.ApiEligibilityResponse
import uk.gov.hmrc.helptosavetestadminfrontend.util.HttpResponseOps.httpResponseOps
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

import scala.concurrent.{ExecutionContext, Future}

class HelpToSaveApiConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends Logging {

  def checkEligibility(nino: String, accessToken: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[String, ApiEligibilityResponse]] = {
    val validHttpHeaders = Seq(
      "Gov-Client-User-ID" → "blah",
      "Gov-Client-Timezone" → "blah",
      "Gov-Vendor-Version" → "blah",
      "Gov-Vendor-Instance-ID" → "blah",
      "Accept" → "application/vnd.hmrc.2.0+json")

    http.get(s"${appConfig.apiUrl}/eligibility/$nino")(hc.copy(authorization = Some(Authorization(s"Bearer $accessToken"))), ec).map { response ⇒
      logger.info(s"checking eligibility for nino=$nino, returned status=${response.status}")
      if (response.status == Status.OK) {
        response.parseJSON[ApiEligibilityResponse](None)
      } else {
        Left(s"status: ${response.status}, body: ${response.body}")
      }
    }.recover {
      case ex ⇒
        Left(ex.getMessage)
    }
  }

}
