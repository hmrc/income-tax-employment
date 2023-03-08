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

import models.api.OtherEmploymentIncome
import support.builders.api.DisabilityBuilder.aDisability
import support.builders.api.ForeignServiceBuilder.aForeignService
import support.builders.api.LumpSumBuilder.aLumpSum
import support.builders.api.ShareAwardedOrReceivedBuilder.anAwardedOrReceivedShare
import support.builders.api.ShareOptionBuilder.aShareOption
import support.utils.TaxYearUtils.taxYearEOY

import java.time.Instant

object OtherEmploymentIncomeBuilder {

  val anOtherEmploymentIncome: OtherEmploymentIncome = OtherEmploymentIncome(
    submittedOn = Some(Instant.parse(s"$taxYearEOY-01-04T05:01:01Z")),
    shareOptions = Some(Set(aShareOption)),
    awardedOrReceivedShares = Some(Set(anAwardedOrReceivedShare)),
    disability = Some(aDisability),
    foreignService = Some(aForeignService),
    lumpSums = Some(Set(aLumpSum))
  )
}
