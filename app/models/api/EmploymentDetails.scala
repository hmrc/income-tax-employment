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

package models.api

import models.shared.{Benefits, Deductions, Pay}
import play.api.libs.json.{Json, OFormat}

case class EmploymentDetails(employmentSequenceNumber: Option[String],
                             payrollId: Option[String],
                             companyDirector: Option[Boolean],
                             closeCompany: Option[Boolean],
                             directorshipCeasedDate: Option[String],
                             startDate: Option[String],
                             cessationDate: Option[String],
                             occPen: Option[Boolean],
                             disguisedRemuneration: Option[Boolean],
                             offPayrollWorker: Option[Boolean],
                             employer: Employer,
                             pay: Option[Pay],
                             deductions: Option[Deductions],
                             benefitsInKind: Option[Benefits])

object EmploymentDetails {
  implicit val formats: OFormat[EmploymentDetails] = Json.format[EmploymentDetails]
}
