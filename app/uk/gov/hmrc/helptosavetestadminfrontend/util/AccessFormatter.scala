/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.helptosavetestadminfrontend.util

import play.api.data.FormError
import play.api.data.format.Formatter

object AccessFormatter {

  val accessFormatter = new Formatter[AccessType] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AccessType] =
      data.get(key).fold[Either[Seq[FormError], AccessType]](
        Left(Seq(FormError(key, "There was no AccessType string found")))){
        case "Privileged"     ⇒ Right(Privileged)
        case "UserRestricted" ⇒ Right(UserRestricted)
        case error            ⇒ Left(Seq(FormError(key, s"Invalid AccessType found, error message: $error")))
      }

    override def unbind(key: String, value: AccessType): Map[String, String] =
      Map(key -> value.toString)
  }

}
