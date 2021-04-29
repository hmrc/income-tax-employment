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

package utils

import org.scalatestplus.play.PlaySpec

class ViewParameterValidationSpec extends PlaySpec {

  "ViewParameterValidation" should {

    "return true if HMRC-HELD is provided" in {
      val result = ViewParameterValidation.isValid("HMRC-HELD")
      result mustBe true
    }

    "return true if CUSTOMER is provided" in {
      val result = ViewParameterValidation.isValid("CUSTOMER")
      result mustBe true
    }

    "return true if LATEST is provided" in {
      val result = ViewParameterValidation.isValid("LATEST")
      result mustBe true
    }

    "return false if not correct param" in {
      val result = ViewParameterValidation.isValid("HMRC_HELD")
      result mustBe false
    }

    "return false if empty" in {
      val result = ViewParameterValidation.isValid("")
      result mustBe false
    }
  }
}