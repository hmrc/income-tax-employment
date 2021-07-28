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

case class Employment(
                       pay: PayModel,
                       lumpSums: Option[LumpSums],
                       deductions: Option[Deductions],
                       benefitsInKind: Option[Benefits]
                     )

object Employment {
  implicit val formats: OFormat[Employment] = Json.format[Employment]
}

case class PayModel(
                     taxablePayToDate: BigDecimal,
                     totalTaxToDate: BigDecimal,
                     tipsAndOtherPayments: Option[BigDecimal]
                   )

object PayModel {
  implicit val formats: OFormat[PayModel] = Json.format[PayModel]
}

case class LumpSums(
                     taxableLumpSumsAndCertainIncome: Option[TaxableLumpSumsAndCertainIncome],
                     benefitFromEmployerFinancedRetirementScheme: Option[BenefitFromEmployerFinancedRetirementScheme],
                     redundancyCompensationPaymentsOverExemption: Option[RedundancyCompensationPaymentsOverExemption],
                     redundancyCompensationPaymentsUnderExemption: Option[RedundancyCompensationPaymentsUnderExemption]
                   )

object LumpSums {
  implicit val formats: OFormat[LumpSums] = Json.format[LumpSums]
}

case class TaxableLumpSumsAndCertainIncome(
                                            amount: BigDecimal,
                                            taxPaid: Option[BigDecimal],
                                            taxTakenOffInEmployment: Option[Boolean]
                                          )

object TaxableLumpSumsAndCertainIncome {
  implicit val formats: OFormat[TaxableLumpSumsAndCertainIncome] = Json.format[TaxableLumpSumsAndCertainIncome]
}

case class BenefitFromEmployerFinancedRetirementScheme(
                                                        amount: BigDecimal,
                                                        exemptAmount: Option[BigDecimal],
                                                        taxPaid: Option[BigDecimal],
                                                        taxTakenOffInEmployment: Option[Boolean]
                                                      )

object BenefitFromEmployerFinancedRetirementScheme {
  implicit val formats: OFormat[BenefitFromEmployerFinancedRetirementScheme] = Json.format[BenefitFromEmployerFinancedRetirementScheme]
}

case class RedundancyCompensationPaymentsOverExemption(
                                                        amount: BigDecimal,
                                                        taxPaid: Option[BigDecimal],
                                                        taxTakenOffInEmployment: Option[Boolean]
                                                      )

object RedundancyCompensationPaymentsOverExemption {
  implicit val formats: OFormat[RedundancyCompensationPaymentsOverExemption] = Json.format[RedundancyCompensationPaymentsOverExemption]
}

case class RedundancyCompensationPaymentsUnderExemption(
                                                         amount: BigDecimal
                                                       )

object RedundancyCompensationPaymentsUnderExemption {
  implicit val formats: OFormat[RedundancyCompensationPaymentsUnderExemption] = Json.format[RedundancyCompensationPaymentsUnderExemption]
}
