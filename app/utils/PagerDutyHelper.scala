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

package utils

import play.api.Logging
import uk.gov.hmrc.http.HttpResponse

object PagerDutyHelper extends Logging {

  object PagerDutyKeys extends Enumeration {
    val BAD_SUCCESS_JSON_FROM_DES: PagerDutyKeys.Value = Value
    val SERVICE_UNAVAILABLE_FROM_DES: PagerDutyKeys.Value = Value
    val INTERNAL_SERVER_ERROR_FROM_DES: PagerDutyKeys.Value = Value
    val UNEXPECTED_RESPONSE_FROM_DES: PagerDutyKeys.Value = Value
    val FOURXX_RESPONSE_FROM_DES: PagerDutyKeys.Value = Value
    val INVALID_TO_REMOVE_PARAMETER_BAD_REQUEST: PagerDutyKeys.Value = Value
  }

  def pagerDutyLog(pagerDutyKey: PagerDutyKeys.Value, otherDetail: String = ""): Unit = {
    logger.error(s"$pagerDutyKey $otherDetail")
  }

  def getCorrelationId(response:HttpResponse): String ={
    response.header("CorrelationId") match {
      case Some(id) => s" CorrelationId: $id"
      case _ => ""
    }
  }
}
