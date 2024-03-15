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
import connectors.parsers.OtherEmploymentIncomeIFHttpParser.{OtherEmploymentIncomeIFHttpReads, OtherEmploymentIncomeIFResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, StringContextOps}
import utils.TaxYearUtils.toTaxYearParam

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherEmploymentIncomeIFConnector @Inject()(val http: HttpClient,
                                                 val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def getOtherEmploymentIncome(nino: String,
                               taxYear: Int)
                              (implicit hc: HeaderCarrier): Future[OtherEmploymentIncomeIFResponse] = {
    val url = url"$baseUrl/income-tax/income/other/employments/${toTaxYearParam(taxYear)}/$nino"

    def call(implicit hc: HeaderCarrier): Future[OtherEmploymentIncomeIFResponse] = {
      http.GET[OtherEmploymentIncomeIFResponse](url)
    }

    call(integrationFrameworkHeaderCarrier(url, GET_OTHER_EMPLOYMENT))
  }
}
