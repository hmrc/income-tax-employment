/*
 * Copyright 2025 HM Revenue & Customs
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

package models.prePopulation

import play.api.libs.json.Json
import support.UnitTest
import support.utils.EmploymnetDataUtils._

class PrePopulationResponseSpec extends UnitTest {

  "PrePopulationResponse" when {
    "written to JSON" should {
      "return the expected JsValue" in {
        val prePopulationResponse = PrePopulationResponse(
          hasEmployment = true
        )

        Json.toJson(prePopulationResponse) shouldBe Json.parse(
        """
           |{
           |  "hasEmployment": true
           |}
        """.stripMargin
        )
      }
    }

    "fromData" should {
      "return hasEmployment as 'false' when HMRC and customer employment data are 'Not available'" in {
        val data = employmentData()
        PrePopulationResponse.fromData(data) shouldBe PrePopulationResponse(false)
      }

      "return hasEmployment as 'true' when customer employment data exists" in {
        val data = employmentData(customerEmploymentData = Seq(employmentSource()))
        PrePopulationResponse.fromData(data) shouldBe PrePopulationResponse(true)
      }

      "return hasEmployment as 'true' when customer data does NOT exist and HMRC employment data exist and dateIgnored is 'None' " in {
        val data = employmentData(
          hmrcEmploymentData =
            Seq(
              hmrcEmploymentSource()
            )
        )
        PrePopulationResponse.fromData(data) shouldBe PrePopulationResponse(true)
      }

      "return hasEmployment as 'false' when customer data does NOT exist and HMRC employment data exist and dateIgnored has value " in {
        val data = employmentData(
          hmrcEmploymentData =
            Seq(
              hmrcEmploymentSource( dateIgnored = Option("some date")
            )
          )
        )
        PrePopulationResponse.fromData(data) shouldBe PrePopulationResponse(false)
      }

      "return hasEmployment as 'true' when customer data does NOT exist and at least one HMRC employment data exist with dateIgnored is 'None' " in {
        val data = employmentData(
          hmrcEmploymentData =
            Seq(
              hmrcEmploymentSource( dateIgnored = Option("some date")),
              hmrcEmploymentSource()
            )
        )
        PrePopulationResponse.fromData(data) shouldBe PrePopulationResponse(true)
      }
    }
  }
}
