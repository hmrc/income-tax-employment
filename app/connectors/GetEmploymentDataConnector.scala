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
import connectors.parsers.GetEmploymentDataHttpParser.{GetEmploymentDataHttpReads, GetEmploymentDataResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearUtils._

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GetEmploymentDataConnector @Inject()(http: HttpClient, val appConfig: AppConfig)
                                          (implicit ec: ExecutionContext) extends IFConnector {
  val specificTaxYear: Int = 2024

  def getEmploymentData(nino: String,
                        taxYear: Int,
                        employmentId: String,
                        view: String)
                       (implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] = {
    val url: URL = getEmploymentDataUrl(nino, taxYear, employmentId, view)

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] = {
      http.GET[GetEmploymentDataResponse](url)
    }

    integrationFrameworkCall(integrationFrameworkHeaderCarrier(url, getApiVersion(taxYear)))
  }

  private def getApiVersion(taxYear: Int): String = {
    if (taxYear >= specificTaxYear) GET_EMPLOYMENT_DATA_23_24 else GET_EMPLOYMENT_DATA
  }

  private def getEmploymentDataUrl(nino: String,
                                   taxYear: Int,
                                   employmentId: String,
                                   view: String): URL = {
    if (taxYear >= specificTaxYear) {
      new URL(s"$baseUrl/income-tax/income/employments/${toTaxYearParam(taxYear)}/$nino/$employmentId?view=$view")
    } else {
      new URL(s"$baseUrl/income-tax/income/employments/$nino/${toTaxYearParam(taxYear)}/$employmentId?view=$view")
    }
  }
}
