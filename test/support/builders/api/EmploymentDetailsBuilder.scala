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
    employer = anEmployer,
    pay = Some(aPay),
    deductions = Some(aDeductions)
  )
}
