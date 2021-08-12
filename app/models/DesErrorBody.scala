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

import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{JsValue, Json, OFormat}

sealed trait DesErrorBody

case class DesErrorModel(status: Int, body: DesErrorBody){
  def toJson: JsValue ={
    body match {
      case error: DesErrorBodyModel => Json.toJson(error)
      case errors: DesErrorsBodyModel => Json.toJson(errors)
    }
  }


}


/** Single DES Error **/
case class DesErrorBodyModel(code: String, reason: String) extends DesErrorBody

object DesErrorBodyModel {
  implicit val formats: OFormat[DesErrorBodyModel] = Json.format[DesErrorBodyModel]
  def parsingError(desAPI: Boolean = true): DesErrorBodyModel = {
    DesErrorBodyModel("PARSING_ERROR", s"Error parsing response from ${if(desAPI) "DES" else "API"}")
  }
  val invalidView: DesErrorBodyModel = DesErrorBodyModel("INVALID_VIEW", "Submission has not passed validation. Invalid query parameter view.")
  private val invalidCreateUpdateRequestBody: DesErrorBodyModel = {
    DesErrorBodyModel("INVALID_CREATE_UPDATE_EMPLOYMENT_REQUEST", "Request to create/update employment is invalid.")
  }
  val invalidCreateUpdateRequest: DesErrorModel = DesErrorModel(BAD_REQUEST,invalidCreateUpdateRequestBody)
}

/** Multiple DES Errors **/
case class DesErrorsBodyModel(failures: Seq[DesErrorBodyModel]) extends DesErrorBody

object DesErrorsBodyModel {
  implicit val formats: OFormat[DesErrorsBodyModel] = Json.format[DesErrorsBodyModel]
}
