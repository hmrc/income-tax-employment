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

package connectors

import config.AppConfig
import connectors.parsers.RefreshSubmissionCacheHttpParser.RefreshSubmissionCacheHttpReads
import models.api.RefreshIncomeSourceRequest
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmissionConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging with Connector {

  def refreshSubmissionCache(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): DownstreamOutcome[Unit] = {
    val url   = appConfig.submissionBaseUrl + s"/income-tax/nino/$nino/sources/session?taxYear=$taxYear"
    val model = RefreshIncomeSourceRequest("employment")

    def call(implicit hc: HeaderCarrier): DownstreamOutcome[Unit] =
      http.PUT[RefreshIncomeSourceRequest, DownstreamErrorOr[Unit]](url, model)

    call(headerCarrier(url = url, extraHeaders = ("mtditid", mtditid)))

  }

}
