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

package utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaxYearUtilSpec extends AnyWordSpec with Matchers{

  "TaxYearUtilSpec" should {

    "return a string containing the two digits of last year and this year when input is specific tax year" in {
      val taxYear = 2024
      val result = TaxYearUtils.toTaxYearParam(taxYear)
      result mustBe "23-24"
    }

    "return a string containing the last year and two digits of this year when input taxYear is string" in {
      val taxYear = 2022
      val result = TaxYearUtils.toTaxYearParam(taxYear)
      result mustBe "2021-22"
    }
  }
}
