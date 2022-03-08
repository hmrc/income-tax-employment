/*
 * Copyright 2022 HM Revenue & Customs
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
import models.DES.DESEmploymentList
import models.frontend.{EmploymentBenefits, EmploymentData, EmploymentFinancialData, HmrcEmploymentSource}
import play.api.libs.json.{JsArray, JsObject, Json}
import utils.TestUtils

class DESEmploymentListSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val jsonModel: JsObject = Json.obj(
    "employments" -> JsArray(
      Seq(
        Json.obj(
          "employmentId" -> "00000000-0000-0000-1111-000000000000",
          "employerRef" -> "666/66666",
          "employerName" -> "Business",
          "payrollId" -> "1234567890",
          "startDate" -> "2020-01-01",
          "cessationDate" -> "2020-01-01",
          "dateIgnored" -> "2020-01-01T10:00:38Z"
        )
      )
    ),
    "customerDeclaredEmployments" -> JsArray(
      Seq(
        Json.obj(
          "employmentId" -> "00000000-0000-0000-2222-000000000000",
          "employerRef" -> "666/66666",
          "employerName" -> "Business",
          "payrollId" -> "1234567890",
          "startDate" -> "2020-01-01",
          "cessationDate" -> "2020-01-01",
          "submittedOn" -> "2020-01-01T10:00:38Z"
        )
      )
    )
  )

  "GetEmploymentListModel" should {

    "parse to Json" in {
      Json.toJson(getEmploymentListModelExample) mustBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[DESEmploymentList]
    }
  }

  ".toHmrcEmploymentSource" should {
    "return a hmrc source with empty financials" in {
      hmrcEmploymentModel.toHmrcEmploymentSource(None,None,None,None) mustBe HmrcEmploymentSource(
        employmentId = hmrcEmploymentModel.employmentId,
        employerName = hmrcEmploymentModel.employerName,
        employerRef = hmrcEmploymentModel.employerRef,
        payrollId = hmrcEmploymentModel.payrollId,
        startDate = hmrcEmploymentModel.startDate,
        cessationDate = hmrcEmploymentModel.cessationDate,
        dateIgnored = hmrcEmploymentModel.dateIgnored,
        submittedOn = None,
        hmrcEmploymentFinancialData = None,
        customerEmploymentFinancialData = None
      )
    }
    "return a hmrc source with hmrc financials" in {
      hmrcEmploymentModel.toHmrcEmploymentSource(Some(hmrcEmploymentDataModelExample),Some(hmrcBenefits),None,None) mustBe HmrcEmploymentSource(
        employmentId = hmrcEmploymentModel.employmentId,
        employerName = hmrcEmploymentModel.employerName,
        employerRef = hmrcEmploymentModel.employerRef,
        payrollId = hmrcEmploymentModel.payrollId,
        startDate = hmrcEmploymentModel.startDate,
        cessationDate = hmrcEmploymentModel.cessationDate,
        dateIgnored = hmrcEmploymentModel.dateIgnored,
        submittedOn = None,
        hmrcEmploymentFinancialData = Some(
          EmploymentFinancialData(
            Some(EmploymentData(hmrcEmploymentDataModelExample)),
            Some(EmploymentBenefits(hmrcBenefits.submittedOn,hmrcBenefits.employment.benefitsInKind))
          )
        ),
        customerEmploymentFinancialData = None
      )
    }
    "return a hmrc source with hmrc and customer financials" in {
      hmrcEmploymentModel.toHmrcEmploymentSource(Some(hmrcEmploymentDataModelExample),Some(hmrcBenefits),
        Some(customerEmploymentDataModelExample),Some(customerBenefits)) mustBe HmrcEmploymentSource(
        employmentId = hmrcEmploymentModel.employmentId,
        employerName = hmrcEmploymentModel.employerName,
        employerRef = hmrcEmploymentModel.employerRef,
        payrollId = hmrcEmploymentModel.payrollId,
        startDate = hmrcEmploymentModel.startDate,
        cessationDate = hmrcEmploymentModel.cessationDate,
        dateIgnored = hmrcEmploymentModel.dateIgnored,
        submittedOn = None,
        hmrcEmploymentFinancialData = Some(
          EmploymentFinancialData(
            Some(EmploymentData(hmrcEmploymentDataModelExample)),
            Some(EmploymentBenefits(hmrcBenefits.submittedOn,hmrcBenefits.employment.benefitsInKind))
          )
        ),
        customerEmploymentFinancialData = Some(
          EmploymentFinancialData(
            Some(EmploymentData(customerEmploymentDataModelExample)),
            Some(EmploymentBenefits(customerBenefits.submittedOn,customerBenefits.employment.benefitsInKind))
          )
        ),
      )
    }
  }

}
