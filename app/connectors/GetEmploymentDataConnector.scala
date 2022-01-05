/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.httpParsers.GetEmploymentDataHttpParser.{GetEmploymentDataHttpReads, GetEmploymentDataResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GetEmploymentDataConnector @Inject()(val http: HttpClient,
                                           val appConfig: AppConfig)(implicit ec:ExecutionContext) extends IFConnector {

  def getEmploymentData(nino: String, taxYear: Int, employmentId: String, view: String)(implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] = {

    val incomeSourcesUri: String =
      appConfig.integrationFrameworkBaseUrl + s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId?view=$view"

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] = {
      http.GET[GetEmploymentDataResponse](incomeSourcesUri)
    }

    integrationFrameworkCall(integrationFrameworkHeaderCarrier(incomeSourcesUri, "1647"))
  }
}
