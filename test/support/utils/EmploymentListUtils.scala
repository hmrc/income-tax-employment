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
import models.api
import models.api.{CustomerEmployment, EmploymentList, HmrcEmployment}

object EmploymentListUtils {
  def employments(
                   employments: Option[Seq[HmrcEmployment]],
                   customerDeclaredEmployments: Option[Seq[CustomerEmployment]]
                 ):EmploymentList =
    EmploymentList(
      employments,
      customerDeclaredEmployments
    )

  val hmrcEmploymentModel: api.HmrcEmployment =
    api.HmrcEmployment(
      employmentId = "00000000-0000-0000-1111-000000000000",
      employerRef = Some("666/66666"),
      employerName = "Business",
      payrollId = Some("1234567890"),
      startDate = Some("2020-01-01"),
      cessationDate = Some("2020-01-01"),
      occupationalPension = Some(false),
      dateIgnored = Some("2020-01-01T10:00:38Z")
    )

  val customerEmploymentModel: api.CustomerEmployment =
    api.CustomerEmployment(
      employmentId = "00000000-0000-0000-2222-000000000000",
      employerRef = Some("666/66666"),
      employerName = "Business",
      payrollId = Some("1234567890"),
      startDate = Some("2020-01-01"),
      cessationDate = Some("2020-01-01"),
      occupationalPension = Some(false),
      submittedOn = "2020-01-01T10:00:38Z"
    )
}
