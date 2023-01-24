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

package connectors.parsers

import connectors.errors.{SingleErrorBody, MultiErrorsBody, ApiError}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys.{BAD_SUCCESS_JSON_FROM_DES, UNEXPECTED_RESPONSE_FROM_DES}
import utils.PagerDutyHelper.{getCorrelationId, pagerDutyLog}

trait DESParser {

  val parserName : String
  val isDesAPI: Boolean

  def logMessage(response:HttpResponse): String ={
    s"[$parserName][read] Received ${response.status} from ${if(isDesAPI) "DES" else "API"}. Body:${response.body}" + getCorrelationId(response)
  }

  def badSuccessJsonFromDES[Response]: Either[ApiError, Response] = {
    pagerDutyLog(BAD_SUCCESS_JSON_FROM_DES, s"[$parserName][read] Invalid Json from ${if(isDesAPI) "DES" else "API"}.")
    Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError(isDesAPI)))
  }

  def handleDESError[Response](response: HttpResponse, statusOverride: Option[Int] = None): Either[ApiError, Response] = {

    val status = statusOverride.getOrElse(response.status)

    try {
      val json = response.json

      lazy val desError = json.asOpt[SingleErrorBody]
      lazy val desErrors = json.asOpt[MultiErrorsBody]

      (desError, desErrors) match {
        case (Some(desError), _) => Left(ApiError(status, desError))
        case (_, Some(desErrors)) => Left(ApiError(status, desErrors))
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_DES, s"[$parserName][read] Unexpected Json from ${if(isDesAPI) "DES" else "API"}.")
          Left(ApiError(status, SingleErrorBody.parsingError(isDesAPI)))
      }
    } catch {
      case _: Exception => Left(ApiError(status, SingleErrorBody.parsingError(isDesAPI)))
    }
  }
}
