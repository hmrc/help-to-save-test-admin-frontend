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

package uk.gov.hmrc.helptosavetestadminfrontend.controllers

import java.net.URL

import cats.syntax.eq._
import cats.instances.string._
import configs.syntax._
import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.helptosavetestadminfrontend.util.Logging
import cats.syntax.either._

import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.util.Try


@Singleton
class MDTPCookieSetterController @Inject()(configuration: Configuration) extends BaseController with Logging  {

  val domainWhitelist: List[String] = configuration.underlying.get[List[String]]("mdtp-cookie-setter.domain-whitelist").value
  val mdtpCookieName = "mdtp"

  def setMDTPCookie(continueAbsoluteURL: String): Action[AnyContent] = Action { implicit request ⇒
    Either.fromTry(Try(new URL(continueAbsoluteURL))).fold[Result](
      { e ⇒
        logger.warn(s"Could not parse continue absolute URL '$continueAbsoluteURL': ${e.getMessage}")
        BadRequest
      }, { url ⇒
        val targetDomain = url.getHost
        if(request.domain === targetDomain){
          logger.info("Request domain same as continue URL domain - proceeding to redirect")
          SeeOther(continueAbsoluteURL)
        } else {
          if (domainWhitelist.contains(targetDomain)) {
            val result = SeeOther(continueAbsoluteURL)

            request.cookies.get(mdtpCookieName).fold {
              logger.info(s"Received request to set mdtp cookie for domain '$targetDomain' but no mdtp cookie was found. Redirecting " +
                "to continue URL without setting any cookies")
              result
            }{ c ⇒
              logger.info(s"Setting mdtp cookie on domain '$targetDomain'")
              result.withCookies(c.copy(domain = Some(targetDomain)))
            }
          } else {
            logger.warn(s"Received request to set cookie for domain '$targetDomain' but it was not whitelisted")
            Forbidden
          }
        }
      })
  }



}
