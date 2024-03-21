/*
 * Copyright 2024 HM Revenue & Customs
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
import models.api.OtherEmploymentIncome
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

object OtherEmploymentIncomeIFHttpParser extends Parser with Logging {

  type OtherEmploymentIncomeIFResponse = Either[ApiError, Option[OtherEmploymentIncome]]

  override val parserName: String = "OtherEmploymentIncomeIFHttpParser"
  override val isDesAPI: Boolean = false

  implicit object OtherEmploymentIncomeIFHttpReads extends HttpReads[OtherEmploymentIncomeIFResponse] {

    override def read(method: String, url: String, response: HttpResponse): OtherEmploymentIncomeIFResponse = {
      response.status match {
        case OK => response.json.validate[OtherEmploymentIncome]
          .fold[OtherEmploymentIncomeIFResponse](
            _ => badSuccessJsonFromDES,
            {
              case OtherEmploymentIncome(None, None, None, None, None, None) => handleDownstreamError(response)
              case otherEmploymentIncomeModel => Right(Some(otherEmploymentIncomeModel))
            }
          )
        case NOT_FOUND =>
          logger.info(logMessage(response))
          Right(None)
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_DES, logMessage(response))
          handleDownstreamError(response)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_DES, logMessage(response))
          handleDownstreamError(response)
        case BAD_REQUEST | UNPROCESSABLE_ENTITY =>
          pagerDutyLog(FOURXX_RESPONSE_FROM_DES, logMessage(response))
          handleDownstreamError(response)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_DES, logMessage(response))
          handleDownstreamError(response, Some(INTERNAL_SERVER_ERROR))
      }
    }
  }
}
