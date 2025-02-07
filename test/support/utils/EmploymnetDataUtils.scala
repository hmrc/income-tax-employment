/*
 * Copyright 2025 HM Revenue & Customs
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

package support.utils
import models.frontend.{AllEmploymentData, EmploymentSource, HmrcEmploymentSource}

object EmploymnetDataUtils {
  def employmentData(
                              hmrcEmploymentData: Seq[HmrcEmploymentSource] = Seq.empty,
                              customerEmploymentData: Seq[EmploymentSource] = Seq.empty
                            ):AllEmploymentData =
    AllEmploymentData(
      hmrcEmploymentData = hmrcEmploymentData,
      hmrcExpenses = None,
      customerEmploymentData = customerEmploymentData,
      customerExpenses = None,
      otherEmploymentIncome = None
    )

  def employmentSource():EmploymentSource =
    EmploymentSource(
      employmentId =  "foo",
      employerName =  "foo",
      employerRef = None,
      payrollId = None,
      startDate = None,
      cessationDate = None,
      dateIgnored = None,
      submittedOn = Option("xxxx"),
      employmentData = None,
      employmentBenefits = None,
      occupationalPension = None
    )

  def hmrcEmploymentSource(dateIgnored:Option[String] = None):HmrcEmploymentSource =
    HmrcEmploymentSource(
      employmentId =  "foo",
      employerName =  "foo",
      employerRef = None,
      payrollId = None,
      startDate = None,
      cessationDate = None,
      dateIgnored = dateIgnored,
      submittedOn = Option("xxxx"),
      hmrcEmploymentFinancialData = None,
      customerEmploymentFinancialData = None,
      occupationalPension= None
    )

}
