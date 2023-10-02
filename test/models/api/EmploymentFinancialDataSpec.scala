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
import play.api.libs.json.{JsObject, Json}
import utils.TestUtils

class EmploymentFinancialDataSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val minJsonModel: JsObject = Json.obj(
    "employment" -> Json.obj(
      "pay" -> Json.obj(
        "taxablePayToDate" -> 100.00,
        "totalTaxToDate" -> 100.00
      )
    )
  )

  val maxJsonModel: JsObject = Json.obj(
    "employment" -> Json.obj(
      "pay" -> Json.obj(
        "taxablePayToDate" -> 100.00,
        "totalTaxToDate" -> 100.00,
        "tipsAndOtherPayments" -> 100.00
      ),
      "deductions" -> Json.obj(
        "studentLoans" -> Json.obj(
          "uglDeductionAmount" -> 100.00,
          "pglDeductionAmount" -> 100.00
        )
      ),
      "benefitsInKind" -> Json.obj(
        "accommodation"-> 100,
        "assets"-> 100,
        "assetTransfer"-> 100,
        "beneficialLoan"-> 100,
        "car"-> 100,
        "carFuel"-> 100,
        "educationalServices"-> 100,
        "entertaining"-> 100,
        "expenses"-> 100,
        "medicalInsurance"-> 100,
        "telephone"-> 100,
        "service"-> 100,
        "taxableExpenses"-> 100,
        "van"-> 100,
        "vanFuel"-> 100,
        "mileage"-> 100,
        "nonQualifyingRelocationExpenses"-> 100,
        "nurseryPlaces"-> 100,
        "otherItems"-> 100,
        "paymentsOnEmployeesBehalf"-> 100,
        "personalIncidentalExpenses"-> 100,
        "qualifyingRelocationExpenses"-> 100,
        "employerProvidedProfessionalSubscriptions"-> 100,
        "employerProvidedServices"-> 100,
        "incomeTaxPaidByDirector"-> 100,
        "travelAndSubsistence"-> 100,
        "vouchersAndCreditCards"-> 100,
        "nonCash"-> 100
      ),
      "offPayrollWorker" -> true
    )
  )

  "DESEmploymentFinancialDataModel" should {

    "parse to Json with the minimum amount of fields required" in {
      Json.toJson(minFinancialData) mustBe minJsonModel
    }

    "parse from Json with the minimum amount of fields required" in {
      minJsonModel.as[EmploymentFinancialData]
    }

    "parse to Json with the maximum amount of fields required" in {
      Json.toJson(maxFinancialData) mustBe maxJsonModel
    }

    "parse from Json with the maximum amount of fields required" in {
      maxJsonModel.as[EmploymentFinancialData]
    }
  }

}
