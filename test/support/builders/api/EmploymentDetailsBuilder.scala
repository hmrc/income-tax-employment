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

import models.api.EmploymentDetails
import models.shared.Benefits
import support.builders.api.EmployerBuilder.anEmployer
import support.builders.shared.DeductionsBuilder.aDeductions
import support.builders.shared.PayBuilder.aPay

object EmploymentDetailsBuilder {

  val anEmploymentDetails: EmploymentDetails = EmploymentDetails(
    employmentSequenceNumber = Some("1003"),
    payrollId = Some("123456789999"),
    companyDirector = Some(false),
    closeCompany = Some(true),
    directorshipCeasedDate = Some("2020-02-12"),
    startDate = Some("2019-04-21"),
    cessationDate = Some("2020-03-11"),
    occPen = Some(false),
    disguisedRemuneration = Some(false),
    offPayrollWorker = Some(false),
    employer = anEmployer,
    pay = Some(aPay),
    deductions = Some(aDeductions),
    benefitsInKind = Some(Benefits(
      accommodation = Some(455.67),
      assets = Some(435.54),
      assetTransfer = Some(24.58),
      beneficialLoan = Some(33.89),
      car = Some(3434.78),
      carFuel = Some(34.56),
      educationalServices = Some(445.67),
      entertaining = Some(434.45),
      expenses = Some(3444.32),
      medicalInsurance = Some(4542.47),
      telephone = Some(243.43),
      service = Some(45.67),
      taxableExpenses = Some(24.56),
      van = Some(56.29),
      vanFuel = Some(14.56),
      mileage = Some(34.23),
      nonQualifyingRelocationExpenses = Some(54.62),
      nurseryPlaces = Some(84.29),
      otherItems = Some(67.67),
      paymentsOnEmployeesBehalf = Some(67.23),
      personalIncidentalExpenses = Some(74.29),
      qualifyingRelocationExpenses = Some(78.24),
      employerProvidedProfessionalSubscriptions = Some(84.56),
      employerProvidedServices = Some(56.34),
      incomeTaxPaidByDirector = Some(67.34),
      travelAndSubsistence = Some(56.89),
      vouchersAndCreditCards = Some(34.9),
      nonCash = Some(23.89)
    ))
  )
}
