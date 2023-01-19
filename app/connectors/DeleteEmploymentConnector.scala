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
import connectors.parsers.DeleteEmploymentHttpParser.{DeleteEmploymentHttpReads, DeleteEmploymentResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteEmploymentConnector @Inject()(val http: HttpClient,
                                          val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def deleteEmployment(nino: String, taxYear: Int, employmentId: String)
                      (implicit hc: HeaderCarrier): Future[DeleteEmploymentResponse] = {
    val uri = new URL(s"$baseUrl/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/custom/$employmentId")

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[DeleteEmploymentResponse] = {
      http.DELETE[DeleteEmploymentResponse](uri)
    }

    integrationFrameworkCall(integrationFrameworkHeaderCarrier(uri, DELETE_EMPLOYMENT))
  }
}
