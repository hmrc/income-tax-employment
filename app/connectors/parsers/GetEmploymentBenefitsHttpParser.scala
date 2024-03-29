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

import connectors.errors.ApiError
import models.api.{DESEmploymentBenefits, EmploymentBenefitsData}
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

object GetEmploymentBenefitsHttpParser extends DESParser with Logging {
  type GetEmploymentBenefitsResponse = Either[ApiError, Option[DESEmploymentBenefits]]

  override val parserName: String = "GetEmploymentBenefitsHttpParser"
  override val isDesAPI: Boolean = false

  implicit object GetEmploymentBenefitsHttpReads extends HttpReads[GetEmploymentBenefitsResponse] {

    override def read(method: String, url: String, response: HttpResponse): GetEmploymentBenefitsResponse = {
      response.status match {
        case OK => response.json.validate[DESEmploymentBenefits].fold[GetEmploymentBenefitsResponse](
          _ => badSuccessJsonFromDES,
          {
            case DESEmploymentBenefits(_,_,_,_,EmploymentBenefitsData(None)) => Right(None)
            case parsedModel => Right(Some(parsedModel))
          }
        )
        case NOT_FOUND =>
          logger.info(logMessage(response))
          Right(None)
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_DES, logMessage(response))
          handleDESError(response)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_DES, logMessage(response))
          handleDESError(response)
        case BAD_REQUEST | UNPROCESSABLE_ENTITY=>
          pagerDutyLog(FOURXX_RESPONSE_FROM_DES, logMessage(response))
          handleDESError(response)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_DES, logMessage(response))
          handleDESError(response, Some(INTERNAL_SERVER_ERROR))
      }
    }
  }

}
