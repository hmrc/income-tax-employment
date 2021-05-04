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

package models.DES

import models.frontend.{EmploymentBenefits, EmploymentData, EmploymentExpenses, EmploymentSource}
import play.api.libs.json.{Json, OFormat}

case class DESEmploymentList(employments: Seq[HmrcEmployment],
                             customerDeclaredEmployments: Seq[CustomerEmployment])

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

  def toEmploymentSource(employmentData: Option[DESEmploymentData],
                         employmentExpenses: Option[DESEmploymentExpenses],
                         employmentBenefits: Option[DESEmploymentBenefits]): EmploymentSource = {
    EmploymentSource(
      employmentId, employerName, employerRef, payrollId, startDate, cessationDate, dateIgnored,
      submittedOn = None,
      employmentData = employmentData.map { e =>
        EmploymentData(
          submittedOn = e.submittedOn,
          employmentSequenceNumber = e.employment.employmentSequenceNumber,
          companyDirector = e.employment.companyDirector,
          closeCompany = e.employment.closeCompany,
          directorshipCeasedDate = e.employment.directorshipCeasedDate,
          occPen = e.employment.occPen,
          disguisedRemuneration = e.employment.disguisedRemuneration,
          pay = e.employment.pay
        )
      },
      employmentExpenses = employmentExpenses.map { e =>
        EmploymentExpenses(
          submittedOn = e.submittedOn,
          e.totalExpenses,
          e.expenses
        )
      },
      employmentBenefits = employmentBenefits.map { e =>
        EmploymentBenefits(
          submittedOn = e.submittedOn,
          benefits = e.employment.benefitsInKind
        )
      }
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
                         employmentExpenses: Option[DESEmploymentExpenses],
                         employmentBenefits: Option[DESEmploymentBenefits]): EmploymentSource = {
    EmploymentSource(
      employmentId, employerName, employerRef, payrollId, startDate, cessationDate,
      dateIgnored = None,
      submittedOn = Some(submittedOn),
      employmentData = employmentData.map {
        e =>
          EmploymentData(
            submittedOn = e.submittedOn,
            employmentSequenceNumber = e.employment.employmentSequenceNumber,
            companyDirector = e.employment.companyDirector,
            closeCompany = e.employment.closeCompany,
            directorshipCeasedDate = e.employment.directorshipCeasedDate,
            occPen = e.employment.occPen,
            disguisedRemuneration = e.employment.disguisedRemuneration,
            pay = e.employment.pay
          )
      },
      employmentExpenses = employmentExpenses.map { e =>
        EmploymentExpenses(
          submittedOn = e.submittedOn,
          e.totalExpenses,
          e.expenses
        )
      },
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
