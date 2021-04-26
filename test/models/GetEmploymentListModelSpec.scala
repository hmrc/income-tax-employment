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

package models

import com.codahale.metrics.SharedMetricRegistries
import play.api.libs.json.{JsArray, JsObject, Json}
import utils.TestUtils

class GetEmploymentListModelSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val jsonModel: JsObject = Json.obj(
    "employments" -> JsArray(
      Seq(
        Json.obj(
          "employmentId" -> "00000000-0000-1000-8000-000000000000",
          "employerRef" -> "123/abc 001<Q>",
          "employerName" -> "Vera Lynn",
          "payrollId" -> "123345657",
          "startDate" -> "2020-06-17",
          "cessationDate" -> "2020-06-17",
          "dateIgnored" -> "2020-06-17T10:53:38Z"
        )
      )
    ),
    "customerDeclaredEmployments" -> JsArray(
      Seq(
        Json.obj(
          "employmentId" -> "00000000-0000-1000-8000-000000000000",
          "employerRef" -> "123/abc 001<Q>",
          "employerName" -> "Vera Lynn",
          "payrollId" -> "123345657",
          "startDate" -> "2020-06-17",
          "cessationDate" -> "2020-06-17",
          "submittedOn" -> "2020-06-17T10:53:38Z"
        )
      )
    )
  )

  "GetEmploymentListModel" should {

    "parse to Json" in {
      Json.toJson(getEmploymentListModelExample) mustBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[GetEmploymentListModel]
    }
  }

}
