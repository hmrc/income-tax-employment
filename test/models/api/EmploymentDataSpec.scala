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

package models.api

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}
import utils.TestData.{customerEmploymentDataModelExample, getEmploymentDataModelOnlyRequiredExample}

class EmploymentDataSpec extends AnyWordSpec {
  SharedMetricRegistries.clear()

  val jsonModel: JsObject = Json.obj(
    "submittedOn" -> "2020-01-04T05:01:01Z",
    "source" -> "CUSTOMER",
    "customerAdded" -> "2020-04-04T01:01:01Z",
    "dateIgnored" -> "2020-04-04T01:01:01Z",
    "employment" -> Json.obj(
      "employmentSequenceNumber" -> "1002",
      "payrollId" -> "123456789999",
      "companyDirector" -> false,
      "closeCompany" -> true,
      "directorshipCeasedDate" -> "2020-02-12",
      "startDate" -> "2019-04-21",
      "cessationDate" -> "2020-03-11",
      "occPen" -> false,
      "disguisedRemuneration" -> false,
      "offPayrollWorker" -> false,
      "employer" -> Json.obj(
        "employerRef" -> "223/AB12399",
        "employerName" -> "Business 1"
      ),
      "pay" -> Json.obj(
        "taxablePayToDate" -> 34234.15,
        "totalTaxToDate" -> 6782.92,
        "payFrequency" -> "CALENDAR MONTHLY",
        "paymentDate" -> "2020-04-23",
        "taxWeekNo" -> 32,
        "taxMonthNo" -> 2
      ),
      "deductions" -> Json.obj(
        "studentLoans" -> Json.obj(
          "uglDeductionAmount" -> 100,
          "pglDeductionAmount" -> 100
        )
      ),
      "benefitsInKind" -> Json.obj("accommodation" -> 100,
        "assets" -> 100,
        "assetTransfer" -> 100,
        "beneficialLoan" -> 100,
        "car" -> 100,
        "carFuel" -> 100,
        "educationalServices" -> 100,
        "entertaining" -> 100,
        "expenses" -> 100,
        "medicalInsurance" -> 100,
        "telephone" -> 100,
        "service" -> 100,
        "taxableExpenses" -> 100,
        "van" -> 100,
        "vanFuel" -> 100,
        "mileage" -> 100,
        "nonQualifyingRelocationExpenses" -> 100,
        "nurseryPlaces" -> 100,
        "otherItems" -> 100,
        "paymentsOnEmployeesBehalf" -> 100,
        "personalIncidentalExpenses" -> 100,
        "qualifyingRelocationExpenses" -> 100,
        "employerProvidedProfessionalSubscriptions" -> 100,
        "employerProvidedServices" -> 100,
        "incomeTaxPaidByDirector" -> 100,
        "travelAndSubsistence" -> 100,
        "vouchersAndCreditCards" -> 100,
        "nonCash" -> 100)
    )
  )

  val jsonModelWithOnlyRequired: JsObject = Json.obj(
    "submittedOn" -> "2020-01-04T05:01:01Z",
    "employment" -> Json.obj(
      "employer" -> Json.obj(
        "employerName" -> "maggie"
      ),
      "pay" -> Json.obj(
        "taxablePayToDate" -> 34234.15,
        "totalTaxToDate" -> 6782.92
      ),
      "benefitsInKind" -> Json.obj("accommodation" -> 100,
        "assets" -> 100,
        "assetTransfer" -> 100,
        "beneficialLoan" -> 100,
        "car" -> 100,
        "carFuel" -> 100,
        "educationalServices" -> 100,
        "entertaining" -> 100,
        "expenses" -> 100,
        "medicalInsurance" -> 100,
        "telephone" -> 100,
        "service" -> 100,
        "taxableExpenses" -> 100,
        "van" -> 100,
        "vanFuel" -> 100,
        "mileage" -> 100,
        "nonQualifyingRelocationExpenses" -> 100,
        "nurseryPlaces" -> 100,
        "otherItems" -> 100,
        "paymentsOnEmployeesBehalf" -> 100,
        "personalIncidentalExpenses" -> 100,
        "qualifyingRelocationExpenses" -> 100,
        "employerProvidedProfessionalSubscriptions" -> 100,
        "employerProvidedServices" -> 100,
        "incomeTaxPaidByDirector" -> 100,
        "travelAndSubsistence" -> 100,
        "vouchersAndCreditCards" -> 100,
        "nonCash" -> 100)
    )
  )

  "GetEmploymentDataModel with all values" should {

    "parse to Json" in {
      Json.toJson(customerEmploymentDataModelExample) mustBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[EmploymentData]
    }
  }

  "GetEmploymentDataModel with only the required values" should {

    "parse to Json" in {
      Json.toJson(getEmploymentDataModelOnlyRequiredExample) mustBe jsonModelWithOnlyRequired
    }

    "parse from Json" in {
      jsonModelWithOnlyRequired.as[EmploymentData]
    }
  }

}
