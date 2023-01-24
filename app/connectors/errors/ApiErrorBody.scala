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

package connectors.errors

import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{Json, OFormat}

sealed trait ApiErrorBody

/** Single DES Error * */
case class SingleErrorBody(code: String, reason: String) extends ApiErrorBody

object SingleErrorBody {
  implicit val formats: OFormat[SingleErrorBody] = Json.format[SingleErrorBody]

  val invalidView: SingleErrorBody = SingleErrorBody("INVALID_VIEW", "Submission has not passed validation. Invalid query parameter view.")
  val invalidCreateUpdateRequest: ApiError =
    ApiError(BAD_REQUEST, SingleErrorBody("INVALID_CREATE_UPDATE_EMPLOYMENT_REQUEST", "Request to create/update employment is invalid."))

  def parsingError(desAPI: Boolean = true): SingleErrorBody = {
    SingleErrorBody("PARSING_ERROR", s"Error parsing response from ${if (desAPI) "DES" else "API"}")
  }
}

/** Multiple DES Errors * */
case class MultiErrorsBody(failures: Seq[SingleErrorBody]) extends ApiErrorBody

object MultiErrorsBody {
  implicit val formats: OFormat[MultiErrorsBody] = Json.format[MultiErrorsBody]
}
