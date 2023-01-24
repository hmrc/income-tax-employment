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

import models._
import play.api.libs.json.{Json, OFormat}

case class CustomerEmployment(employmentId: String,
                              employerRef: Option[String],
                              employerName: String,
                              payrollId: Option[String],
                              startDate: Option[String],
                              cessationDate: Option[String],
                              occupationalPension: Option[Boolean],
                              submittedOn: String) {

  def toEmploymentSource(employmentData: Option[EmploymentData],
                         employmentBenefits: Option[DESEmploymentBenefits]): frontend.EmploymentSource = {

    val _employerRef: Option[String] = if (employerRef.isDefined) employerRef else employmentData.flatMap(_.employment.employer.employerRef)
    val _payrollId: Option[String] = if (payrollId.isDefined) payrollId else employmentData.flatMap(_.employment.payrollId)
    val _startDate: Option[String] = if (startDate.isDefined) startDate else employmentData.flatMap(_.employment.startDate)
    val _cessationDate: Option[String] = if (cessationDate.isDefined) cessationDate else employmentData.flatMap(_.employment.cessationDate)

    frontend.EmploymentSource(
      employmentId, employerName, _employerRef, _payrollId, _startDate, _cessationDate,
      dateIgnored = None,
      occupationalPension = occupationalPension,
      submittedOn = Some(submittedOn),
      employmentData = employmentData.map(frontend.EmploymentData(_)),
      employmentBenefits = employmentBenefits.map { e =>
        frontend.EmploymentBenefits(
          submittedOn = e.submittedOn,
          benefits = e.employment.benefitsInKind
        )
      }
    )
  }
}

object CustomerEmployment {
  implicit val formats: OFormat[CustomerEmployment] = Json.format[CustomerEmployment]
}
