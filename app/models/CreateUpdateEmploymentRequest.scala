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

package models

import models.api.{EmploymentFinancialData, Employment, PayModel}
import models.shared.{Benefits, CreateUpdateEmployment, Deductions}
import play.api.libs.json.{Json, OFormat}

case class CreateUpdateEmploymentRequest(employmentId: Option[String] = None,
                                         employment: Option[CreateUpdateEmployment] = None,
                                         employmentData: Option[CreateUpdateEmploymentData] = None,
                                         hmrcEmploymentIdToIgnore: Option[String] = None,
                                         isHmrcEmploymentId: Option[Boolean] = None)

object CreateUpdateEmploymentRequest {
  implicit val formats: OFormat[CreateUpdateEmploymentRequest] = Json.format[CreateUpdateEmploymentRequest]
}

case class CreateUpdateEmploymentData(pay: PayModel,
                                      deductions: Option[Deductions] = None,
                                      benefitsInKind: Option[Benefits] = None){
  def toDESModel: EmploymentFinancialData = {
    EmploymentFinancialData(employment = Employment(
      pay = pay,
      deductions = deductions,
      benefitsInKind = benefitsInKind
    ))
  }
}

object CreateUpdateEmploymentData {
  implicit val formats: OFormat[CreateUpdateEmploymentData] = Json.format[CreateUpdateEmploymentData]
}
