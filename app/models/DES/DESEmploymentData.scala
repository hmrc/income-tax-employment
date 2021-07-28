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

package models.DES

import models.shared.{Deductions, Pay}
import play.api.libs.json.{Json, OFormat}

case class DESEmploymentData(submittedOn: String,
                             source: Option[String],
                             customerAdded: Option[String],
                             dateIgnored: Option[String],
                             employment: DESEmploymentDetails)

object DESEmploymentData {
  implicit val formats: OFormat[DESEmploymentData] = Json.format[DESEmploymentData]
}

case class DESEmploymentDetails(employmentSequenceNumber: Option[String],
                                payrollId: Option[String],
                                companyDirector: Option[Boolean],
                                closeCompany: Option[Boolean],
                                directorshipCeasedDate: Option[String],
                                startDate: Option[String],
                                cessationDate: Option[String],
                                occPen: Option[Boolean],
                                disguisedRemuneration: Option[Boolean],
                                employer: Employer,
                                pay: Option[Pay],
                                deductions: Option[Deductions]
                               )

object DESEmploymentDetails {
  implicit val formats: OFormat[DESEmploymentDetails] = Json.format[DESEmploymentDetails]
}

case class Employer(employerRef: Option[String],
                    employerName: String)

object Employer {
  implicit val formats: OFormat[Employer] = Json.format[Employer]
}
