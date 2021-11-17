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

import models.shared.{Benefits, Deductions}
import play.api.libs.json.{Json, OFormat}

case class DESEmploymentFinancialData(employment: Employment)

object DESEmploymentFinancialData {
  implicit val formats: OFormat[DESEmploymentFinancialData] = Json.format[DESEmploymentFinancialData]
}

case class Employment(pay: PayModel,
                      deductions: Option[Deductions],
                      benefitsInKind: Option[Benefits])

object Employment {
  implicit val formats: OFormat[Employment] = Json.format[Employment]
}

case class PayModel(taxablePayToDate: BigDecimal,
                    totalTaxToDate: BigDecimal,
                    tipsAndOtherPayments: Option[BigDecimal])

object PayModel {
  implicit val formats: OFormat[PayModel] = Json.format[PayModel]
}
