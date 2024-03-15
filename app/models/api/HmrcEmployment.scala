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

case class HmrcEmployment(employmentId: String,
                          employerRef: Option[String],
                          employerName: String,
                          payrollId: Option[String],
                          startDate: Option[String],
                          cessationDate: Option[String],
                          occupationalPension: Option[Boolean],
                          dateIgnored: Option[String]) {

  private def toEmploymentFinancialData(employmentData: Option[EmploymentData]): frontend.EmploymentFinancialData = {
    frontend.EmploymentFinancialData(
      employmentData = employmentData.map(frontend.EmploymentData(_)),
      employmentBenefits = employmentData.filter(_.employment.benefitsInKind.isDefined).map { e =>
        frontend.EmploymentBenefits(
          submittedOn = e.submittedOn,
          benefits = e.employment.benefitsInKind
        )
      }
    )
  }

  def toHmrcEmploymentSource(hmrcEmploymentData: Option[EmploymentData],
                             customerEmploymentData: Option[EmploymentData]): frontend.HmrcEmploymentSource = {

    val hmrcFinancials: Option[frontend.EmploymentFinancialData] = if (hmrcEmploymentData.isDefined) {
      Some(toEmploymentFinancialData(hmrcEmploymentData))
    } else {
      None
    }

    val customerFinancials: Option[frontend.EmploymentFinancialData] = if (customerEmploymentData.isDefined) {
      Some(toEmploymentFinancialData(customerEmploymentData))
    } else {
      None
    }

    val _employerRef: Option[String] = if (employerRef.isDefined) employerRef else hmrcEmploymentData.flatMap(_.employment.employer.employerRef)
    val _payrollId: Option[String] = if (payrollId.isDefined) payrollId else hmrcEmploymentData.flatMap(_.employment.payrollId)
    val _startDate: Option[String] = if (startDate.isDefined) startDate else hmrcEmploymentData.flatMap(_.employment.startDate)
    val _cessationDate: Option[String] = if (cessationDate.isDefined) cessationDate else hmrcEmploymentData.flatMap(_.employment.cessationDate)
    val _dateIgnored: Option[String] = if (dateIgnored.isDefined) dateIgnored else hmrcEmploymentData.flatMap(_.dateIgnored)

    frontend.HmrcEmploymentSource(
      employmentId, employerName, _employerRef, _payrollId, _startDate, _cessationDate, _dateIgnored,
      submittedOn = None,
      hmrcEmploymentFinancialData = hmrcFinancials,
      customerEmploymentFinancialData = customerFinancials,
      occupationalPension = occupationalPension
    )
  }
}

object HmrcEmployment {
  implicit val formats: OFormat[HmrcEmployment] = Json.format[HmrcEmployment]
}
