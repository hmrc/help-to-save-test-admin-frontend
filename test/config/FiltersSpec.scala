/*
 * Copyright 2021 HM Revenue & Customs
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

package config

import controllers.TestSupport
import play.api.Configuration
import uk.gov.hmrc.helptosavetestadminfrontend.config.{AllowListFilter, Filters}
import uk.gov.hmrc.play.bootstrap.frontend.filters.SessionIdFilter
import uk.gov.hmrc.play.bootstrap.filters._

class FiltersSpec extends TestSupport {

  val mockMDCFilter = new MDCFilter(fakeApplication.materializer, fakeApplication.configuration, "")
  val mockAllowlistFilter = mock[uk.gov.hmrc.play.bootstrap.frontend.filters.AllowlistFilter]

  val mockSessionIdFilter =mock[SessionIdFilter]

  val allowListFilter   = mock[AllowListFilter]

  "Filters" must {

    "include the allowList filter if the allowList from config is non empty" in {
      val config = Configuration("http-header-ip-whitelist" → List("1.2.3"))

      val filters = new Filters(config, allowListFilter)
      filters.filters shouldBe Seq(allowListFilter)
    }

    "not include the allowList filter if the allowList from config is empty" in {
      val config = Configuration("http-header-ip-whitelist" → List())

      val filters = new Filters(config, allowListFilter)
      filters.filters shouldBe Seq()
    }
  }

}
