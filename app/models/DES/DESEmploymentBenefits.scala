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

package models.DES

import models.shared.Benefits
import play.api.libs.json.{Json, OFormat}

case class EmploymentBenefitsData(benefitsInKind: Option[Benefits] = None)

object EmploymentBenefitsData {
  implicit val formats: OFormat[EmploymentBenefitsData] = Json.format[EmploymentBenefitsData]
}

case class DESEmploymentBenefits(submittedOn: String,
                                 customerAdded: Option[String] = None,
                                 dateIgnored: Option[String] = None,
                                 source: Option[String] = None,
                                 employment: EmploymentBenefitsData)

object DESEmploymentBenefits {
  implicit val formats: OFormat[DESEmploymentBenefits] = Json.format[DESEmploymentBenefits]
}
