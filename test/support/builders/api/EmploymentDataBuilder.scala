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

import models.api.EmploymentData
import support.builders.api.EmploymentDetailsBuilder.anEmploymentDetails

object EmploymentDataBuilder {

  val anEmploymentData: EmploymentData = EmploymentData(
    submittedOn = "2020-01-04T05:01:01Z",
    source = Some("CUSTOMER"),
    customerAdded = Some("2020-04-04T01:01:01Z"),
    dateIgnored = Some("2020-04-04T01:01:01Z"),
    employment = anEmploymentDetails
  )

  val anEmploymentDataJson: String =
    """
      {
      |  "submittedOn": "2020-01-04T05:01:01Z",
      |  "source": "CUSTOMER",
      |  "customerAdded": "2020-04-04T01:01:01Z",
      |  "dateIgnored": "2020-04-04T01:01:01Z",
      |  "employment": {
      |    "employmentSequenceNumber": "1003",
      |    "payrollId": "123456789999",
      |    "companyDirector": false,
      |    "closeCompany": true,
      |    "directorshipCeasedDate": "2020-02-12",
      |    "startDate": "2019-04-21",
      |    "cessationDate": "2020-03-11",
      |    "occPen": false,
      |    "disguisedRemuneration": false,
      |    "employer": {
      |      "employerRef": "223/AB12399",
      |      "employerName": "maggie"
      |    },
      |    "pay": {
      |      "taxablePayToDate": 34234.15,
      |      "totalTaxToDate": 6782.92,
      |      "tipsAndOtherPayments": 67676,
      |      "payFrequency": "CALENDAR MONTHLY",
      |      "paymentDate": "2020-04-23",
      |      "taxWeekNo": 32,
      |      "taxMonthNo": 2
      |    },
      |    "deductions": {
      |      "studentLoans": {
      |        "uglDeductionAmount": 13343.45,
      |        "pglDeductionAmount": 24242.56
      |      }
      |    },
      |    "benefitsInKind": {
      |      "accommodation": 455.67,
      |      "assets": 435.54,
      |      "assetTransfer": 24.58,
      |      "beneficialLoan": 33.89,
      |      "car": 3434.78,
      |      "carFuel": 34.56,
      |      "educationalServices": 445.67,
      |      "entertaining": 434.45,
      |      "expenses": 3444.32,
      |      "medicalInsurance": 4542.47,
      |      "telephone": 243.43,
      |      "service": 45.67,
      |      "taxableExpenses": 24.56,
      |      "van": 56.29,
      |      "vanFuel": 14.56,
      |      "mileage": 34.23,
      |      "nonQualifyingRelocationExpenses": 54.62,
      |      "nurseryPlaces": 84.29,
      |      "otherItems": 67.67,
      |      "paymentsOnEmployeesBehalf": 67.23,
      |      "personalIncidentalExpenses": 74.29,
      |      "qualifyingRelocationExpenses": 78.24,
      |      "employerProvidedProfessionalSubscriptions": 84.56,
      |      "employerProvidedServices": 56.34,
      |      "incomeTaxPaidByDirector": 67.34,
      |      "travelAndSubsistence": 56.89,
      |      "vouchersAndCreditCards": 34.9,
      |      "nonCash": 23.89
      |    }
      |  }
      |}
      |""".stripMargin
}
