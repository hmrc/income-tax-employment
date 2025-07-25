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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec

class DESTaxYearHelperSpec extends AnyWordSpec {

  "DESTaxYearHelper" should {

    "return a string containing the last year and the last two digits of this year" in {
      val taxYear = 2020
      val result = DESTaxYearHelper.desTaxYearConverter(taxYear)
      result mustBe "2019-20"
    }

    "return a string containing the last year and the last two digits of this year for a tax year ending in 00" in {
      val taxYear = 2100
      val result = DESTaxYearHelper.desTaxYearConverter(taxYear)
      result mustBe "2099-00"
    }
  }
}
