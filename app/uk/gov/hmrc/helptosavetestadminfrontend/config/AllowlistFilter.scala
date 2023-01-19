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

package uk.gov.hmrc.helptosavetestadminfrontend.config

import akka.stream.Materializer
import com.google.inject.Inject
import configs.syntax._
import play.api.Configuration
import play.api.mvc.{Call, RequestHeader, Result, Results}
import uk.gov.hmrc.helptosavetestadminfrontend.controllers.routes
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import uk.gov.hmrc.allowlist.AkamaiAllowlistFilter

import scala.concurrent.Future

class AllowListFilter @Inject()(configuration: Configuration, val mat: Materializer) extends AkamaiAllowlistFilter with Logging {

  override def allowlist: Seq[String] =
    configuration.underlying.get[List[String]]("http-header-ip-whitelist").value

  override def excludedPaths: Seq[Call] = Seq(forbiddenCall)

  // This is the `Call` used in the `Redirect` when an IP is present in the header
  // of the HTTP request but is not in the allowList
  override def destination: Call = forbiddenCall

  override def noHeaderAction(f:  (RequestHeader) ⇒ Future[Result],
                              rh: RequestHeader): Future[Result] = {
    logger.warn("SuspiciousActivity: No client IP found in http request header")
    Future.successful(Results.Redirect(forbiddenCall))
  }

  val forbiddenCall: Call = Call("GET", routes.ForbiddenController.forbidden.url)

  override def apply(f: (RequestHeader) ⇒ Future[Result])(rh: RequestHeader): Future[Result] = {
    rh.headers.get(trueClient).foreach{ ip ⇒
      if (!allowlist.contains(ip)) {
        logger.warn(s"SuspiciousActivity: Received request from non-allowListed ip $ip")
      }
    }
    super.apply(f)(rh)
  }

}
