/*
 * Copyright 2022 HM Revenue & Customs
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

package models.frontend

import play.api.libs.json.{Json, OFormat}

case class AllEmploymentData(hmrcEmploymentData: Seq[HmrcEmploymentSource],
                             hmrcExpenses: Option[EmploymentExpenses],
                             customerEmploymentData: Seq[EmploymentSource],
                             customerExpenses: Option[EmploymentExpenses])

object AllEmploymentData {
  implicit val format: OFormat[AllEmploymentData] = Json.format[AllEmploymentData]
}

case class EmploymentSource(employmentId: String,
                            employerName: String,
                            employerRef: Option[String],
                            payrollId: Option[String],
                            startDate: Option[String],
                            cessationDate: Option[String],
                            dateIgnored: Option[String],
                            submittedOn: Option[String],
                            employmentData: Option[EmploymentData],
                            employmentBenefits: Option[EmploymentBenefits],
                            occupationalPension: Option[Boolean])

object EmploymentSource {
  implicit val format: OFormat[EmploymentSource] = Json.format[EmploymentSource]
}

case class HmrcEmploymentSource(employmentId: String,
                                employerName: String,
                                employerRef: Option[String],
                                payrollId: Option[String],
                                startDate: Option[String],
                                cessationDate: Option[String],
                                dateIgnored: Option[String],
                                submittedOn: Option[String],
                                hmrcEmploymentFinancialData: Option[EmploymentFinancialData],
                                customerEmploymentFinancialData: Option[EmploymentFinancialData],
                                occupationalPension: Option[Boolean])

object HmrcEmploymentSource {
  implicit val format: OFormat[HmrcEmploymentSource] = Json.format[HmrcEmploymentSource]
}

case class EmploymentFinancialData(employmentData: Option[EmploymentData], employmentBenefits: Option[EmploymentBenefits])

object EmploymentFinancialData {
  implicit val format: OFormat[EmploymentFinancialData] = Json.format[EmploymentFinancialData]
}

