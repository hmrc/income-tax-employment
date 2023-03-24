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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils.jsonObjNoNulls

import java.time.Instant


case class OtherEmploymentsIncome(submittedOn: Option[Instant] = None,
                                  shareOptions: Option[Set[ShareOption]] = None,
                                  awardedOrReceivedShares: Option[Set[AwardedOrReceivedShare]] = None,
                                  disability: Option[Disability] = None,
                                  foreignService: Option[ForeignService] = None,
                                  lumpSums: Option[Set[LumpSum]] = None)

object OtherEmploymentsIncome {
  implicit val otherEmploymentsIncomeWrites: OWrites[OtherEmploymentsIncome] = (otherEmploymentIncome: OtherEmploymentsIncome) => {
    jsonObjNoNulls(
      "submittedOn" -> otherEmploymentIncome.submittedOn,
      "shareOption" -> otherEmploymentIncome.shareOptions,
      "sharesAwardedOrReceived" -> otherEmploymentIncome.awardedOrReceivedShares,
      "disability" -> otherEmploymentIncome.disability,
      "foreignService" -> otherEmploymentIncome.foreignService,
      "lumpSums" -> otherEmploymentIncome.lumpSums
    )
  }

  implicit val otherEmploymentsIncomeReads: Reads[OtherEmploymentsIncome] = (
    (JsPath \ "submittedOn").readNullable[Instant] and
      (JsPath \ "shareOption").readNullable[Set[ShareOption]] and
      (JsPath \ "sharesAwardedOrReceived").readNullable[Set[AwardedOrReceivedShare]] and
      (JsPath \ "disability").readNullable[Disability] and
      (JsPath \ "foreignService").readNullable[ForeignService] and
      (JsPath \ "lumpSums").readNullable[Set[LumpSum]]
    )(OtherEmploymentsIncome.apply _)
}
