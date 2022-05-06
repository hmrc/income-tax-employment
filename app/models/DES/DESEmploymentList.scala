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

package models.DES

import models.frontend.{EmploymentBenefits, EmploymentData, EmploymentFinancialData, EmploymentSource, HmrcEmploymentSource}
import play.api.libs.json.{Json, OFormat}

case class DESEmploymentList(employments: Option[Seq[HmrcEmployment]],
                             customerDeclaredEmployments: Option[Seq[CustomerEmployment]])

object DESEmploymentList {
  implicit val formats: OFormat[DESEmploymentList] = Json.format[DESEmploymentList]
}

case class HmrcEmployment(employmentId: String,
                          employerRef: Option[String],
                          employerName: String,
                          payrollId: Option[String],
                          startDate: Option[String],
                          cessationDate: Option[String],
                          occupationalPension: Option[Boolean],
                          dateIgnored: Option[String]) {

  private def toEmploymentFinancialData(employmentData: Option[DESEmploymentData],
                                        employmentBenefits: Option[DESEmploymentBenefits]): EmploymentFinancialData ={
    EmploymentFinancialData(
      employmentData = employmentData.map(EmploymentData(_)),
      employmentBenefits = employmentBenefits.map { e =>
        EmploymentBenefits(
          submittedOn = e.submittedOn,
          benefits = e.employment.benefitsInKind
        )
      }
    )
  }

  def toHmrcEmploymentSource(hmrcEmploymentData: Option[DESEmploymentData],
                             hmrcEmploymentBenefits: Option[DESEmploymentBenefits],
                             customerEmploymentData: Option[DESEmploymentData],
                             customerEmploymentBenefits: Option[DESEmploymentBenefits]): HmrcEmploymentSource = {

    val hmrcFinancials: Option[EmploymentFinancialData] = if (hmrcEmploymentData.isDefined || hmrcEmploymentBenefits.isDefined) {
      Some(toEmploymentFinancialData(hmrcEmploymentData, hmrcEmploymentBenefits))
    } else {
      None
    }

    val customerFinancials: Option[EmploymentFinancialData] = if(customerEmploymentData.isDefined || customerEmploymentBenefits.isDefined){
      Some(toEmploymentFinancialData(customerEmploymentData,customerEmploymentBenefits))
    } else {
      None
    }

    val _employerRef: Option[String] = if(employerRef.isDefined) employerRef else hmrcEmploymentData.flatMap(_.employment.employer.employerRef)
    val _payrollId: Option[String] = if(payrollId.isDefined) payrollId else hmrcEmploymentData.flatMap(_.employment.payrollId)
    val _startDate: Option[String] = if(startDate.isDefined) startDate else hmrcEmploymentData.flatMap(_.employment.startDate)
    val _cessationDate: Option[String] = if(cessationDate.isDefined) cessationDate else hmrcEmploymentData.flatMap(_.employment.cessationDate)
    val _dateIgnored: Option[String] = if(dateIgnored.isDefined) dateIgnored else hmrcEmploymentData.flatMap(_.dateIgnored)

    HmrcEmploymentSource(
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

case class CustomerEmployment(employmentId: String,
                              employerRef: Option[String],
                              employerName: String,
                              payrollId: Option[String],
                              startDate: Option[String],
                              cessationDate: Option[String],
                              occupationalPension: Option[Boolean],
                              submittedOn: String) {

  def toEmploymentSource(employmentData: Option[DESEmploymentData],
                         employmentBenefits: Option[DESEmploymentBenefits]): EmploymentSource = {

    val _employerRef: Option[String] = if(employerRef.isDefined) employerRef else employmentData.flatMap(_.employment.employer.employerRef)
    val _payrollId: Option[String] = if(payrollId.isDefined) payrollId else employmentData.flatMap(_.employment.payrollId)
    val _startDate: Option[String] = if(startDate.isDefined) startDate else employmentData.flatMap(_.employment.startDate)
    val _cessationDate: Option[String] = if(cessationDate.isDefined) cessationDate else employmentData.flatMap(_.employment.cessationDate)

    EmploymentSource(
      employmentId, employerName, _employerRef, _payrollId, _startDate, _cessationDate,
      dateIgnored = None,
      occupationalPension = occupationalPension,
      submittedOn = Some(submittedOn),
      employmentData = employmentData.map(EmploymentData(_)),
      employmentBenefits = employmentBenefits.map { e =>
        EmploymentBenefits(
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
