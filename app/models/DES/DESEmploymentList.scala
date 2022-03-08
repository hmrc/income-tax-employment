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

    HmrcEmploymentSource(
      employmentId, employerName, employerRef, payrollId, startDate, cessationDate, dateIgnored,
      submittedOn = None,
      hmrcEmploymentFinancialData = hmrcFinancials,
      customerEmploymentFinancialData = customerFinancials
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
                              submittedOn: String) {

  def toEmploymentSource(employmentData: Option[DESEmploymentData],
                         employmentBenefits: Option[DESEmploymentBenefits]): EmploymentSource = {
    EmploymentSource(
      employmentId, employerName, employerRef, payrollId, startDate, cessationDate,
      dateIgnored = None,
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
