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

package connectors

import config.AppConfig
import connectors.parsers.DeleteEmploymentFinancialDataHttpParser.{DeleteEmploymentFinancialDataHttpReads, DeleteEmploymentFinancialDataResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.DESTaxYearHelper.desTaxYearConverter
import utils.TaxYearUtils.{isAfter2324Api, toTaxYearParam}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteEmploymentFinancialDataConnector @Inject()(val http: HttpClientV2,
                                                       val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def deleteEmploymentFinancialData(nino: String, taxYear: Int, employmentId: String)
                                   (implicit hc: HeaderCarrier): Future[DeleteEmploymentFinancialDataResponse] = {

    val (url, apiVersion) = if (isAfter2324Api(taxYear)) {
      (url"$baseUrl/income-tax/${toTaxYearParam(taxYear)}/income/employments/$nino/$employmentId", DELETE_EMPLOYMENT_FINANCIAL_DATA_23_24)
    } else {
      (url"$baseUrl/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId", DELETE_EMPLOYMENT_FINANCIAL_DATA)
    }

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[DeleteEmploymentFinancialDataResponse] =
      http.delete(url).execute

    integrationFrameworkCall(integrationFrameworkHeaderCarrier(url, apiVersion))
  }
}
