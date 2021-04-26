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

package models

import play.api.libs.json.{Json, OFormat}

case class GetEmploymentListModel(
                                   employments: Seq[HmrcEmploymentModel],
                                   customerDeclaredEmployments: Seq[CustomerEmploymentModel]
                                 )

object GetEmploymentListModel {
  implicit val formats: OFormat[GetEmploymentListModel] = Json.format[GetEmploymentListModel]
}

case class HmrcEmploymentModel(
                                employmentId: String,
                                employerRef: Option[String],
                                employerName: String,
                                payrollId: Option[String],
                                startDate: Option[String],
                                cessationDate: Option[String],
                                dateIgnored: Option[String]
                              )

object HmrcEmploymentModel {
  implicit val formats: OFormat[HmrcEmploymentModel] = Json.format[HmrcEmploymentModel]
}

case class CustomerEmploymentModel(
                                    employmentId: String,
                                    employerRef: Option[String],
                                    employerName: String,
                                    payrollId: Option[String],
                                    startDate: Option[String],
                                    cessationDate: Option[String],
                                    submittedOn: String
                                  )

object CustomerEmploymentModel {
  implicit val formats: OFormat[CustomerEmploymentModel] = Json.format[CustomerEmploymentModel]
}

