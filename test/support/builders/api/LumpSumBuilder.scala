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

package support.builders.api

import models.api.LumpSum
import support.builders.api.BenefitFromEmployerFinancedRetirementSchemeBuilder.aBenefitFromEmployerFinancedRetirementScheme
import support.builders.api.RedundancyCompensationPaymentsOverExemptionBuilder.aRedundancyCompensationPaymentsOverExemption
import support.builders.api.RedundancyCompensationPaymentsUnderExemptionBuilder.aRedundancyCompensationPaymentsUnderExemption
import support.builders.api.TaxableLumpSumsAndCertainIncomeBuilder.aTaxableLumpSumsAndCertainIncome

object LumpSumBuilder {

  val aLumpSum: LumpSum = LumpSum(
    employerName = "ABC Ltd",
    employerRef = "321/AB156",
    taxableLumpSumsAndCertainIncome = Some(aTaxableLumpSumsAndCertainIncome),
    benefitFromEmployerFinancedRetirementScheme = Some(aBenefitFromEmployerFinancedRetirementScheme),
    redundancyCompensationPaymentsOverExemption = Some(aRedundancyCompensationPaymentsOverExemption),
    redundancyCompensationPaymentsUnderExemption = Some(aRedundancyCompensationPaymentsUnderExemption),
  )
}
