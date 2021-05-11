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
import models.frontend.AllEmploymentData
import play.api.libs.json.{JsObject, Json}
import utils.TestUtils

class AllEmploymentDataModelSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val jsonModel: JsObject = Json.obj(
      "hmrcEmploymentData" -> Json.arr(
        Json.obj(
          "employmentId" -> "00000000-0000-0000-1111-000000000000",
          "employerName" -> "Business",
          "employerRef" -> "666/66666",
          "payrollId" -> "1234567890",
          "startDate" -> "2020-01-01",
          "cessationDate" -> "2020-01-01",
          "dateIgnored" -> "2020-01-01T10:00:38Z",
          "employmentData" -> Json.obj(
            "submittedOn" -> "2020-01-04T05:01:01Z",
            "employmentSequenceNumber" -> "1002",
            "companyDirector" -> false,
            "closeCompany" -> true,
            "directorshipCeasedDate" -> "2020-02-12",
            "occPen" -> false,
            "disguisedRemuneration" -> false,
            "pay" -> Json.obj(
              "taxablePayToDate" -> 34234.15,
              "totalTaxToDate" -> 6782.92,
              "tipsAndOtherPayments" -> 67676,
              "payFrequency" -> "CALENDAR MONTHLY",
              "paymentDate" -> "2020-04-23",
              "taxWeekNo" -> 32,
              "taxMonthNo" -> 2
            )
          ),
          "employmentBenefits" -> Json.obj(
            "submittedOn" -> "2020-01-04T05:01:01Z",
            "benefits" -> Json.obj(
              "accommodation" -> 100,
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
              "nonCash" -> 100
            )
          )
        )),
    "hmrcExpenses" -> Json.obj(
      "submittedOn" -> "2020-01-04T05:01:01Z",
      "totalExpenses" -> 800,
      "expenses" -> Json.obj(
        "businessTravelCosts" -> 100,
        "jobExpenses" -> 100,
        "flatRateJobExpenses" -> 100,
        "professionalSubscriptions" -> 100,
        "hotelAndMealExpenses" -> 100,
        "otherAndCapitalAllowances" -> 100,
        "vehicleExpenses" -> 100,
        "mileageAllowanceRelief" -> 100
      )
    ),
      "customerEmploymentData" -> Json.arr(
        Json.obj(
          "employmentId" -> "00000000-0000-0000-2222-000000000000",
          "employerName" -> "Business",
          "employerRef" -> "666/66666",
          "payrollId" -> "1234567890",
          "startDate" -> "2020-01-01",
          "cessationDate" -> "2020-01-01",
          "submittedOn" -> "2020-01-01T10:00:38Z",
          "employmentData" -> Json.obj(
            "submittedOn" -> "2020-01-04T05:01:01Z",
            "employmentSequenceNumber" -> "1002",
            "companyDirector" -> false,
            "closeCompany" -> true,
            "directorshipCeasedDate" -> "2020-02-12",
            "occPen" -> false,
            "disguisedRemuneration" -> false,
            "pay" -> Json.obj(
              "taxablePayToDate" -> 34234.15,
              "totalTaxToDate" -> 6782.92,
              "tipsAndOtherPayments" -> 67676,
              "payFrequency" -> "CALENDAR MONTHLY",
              "paymentDate" -> "2020-04-23",
              "taxWeekNo" -> 32,
              "taxMonthNo" -> 2
            )
          ),
          "employmentBenefits" -> Json.obj(
            "submittedOn" -> "2020-01-04T05:01:01Z",
            "benefits" -> Json.obj(
              "accommodation" -> 100,
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
              "nonCash" -> 100
            )
          )
        )),
    "customerExpenses" -> Json.obj(
      "submittedOn" -> "2020-01-04T05:01:01Z",
      "totalExpenses" -> 800,
      "expenses" -> Json.obj(
        "businessTravelCosts" -> 100,
        "jobExpenses" -> 100,
        "flatRateJobExpenses" -> 100,
        "professionalSubscriptions" -> 100,
        "hotelAndMealExpenses" -> 100,
        "otherAndCapitalAllowances" -> 100,
        "vehicleExpenses" -> 100,
        "mileageAllowanceRelief" -> 100
      )
    )
  )

  "AllEmploymentData" should {

    "parse to Json" in {
      Json.toJson(allEmploymentData) mustBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[AllEmploymentData]
    }
  }

}
