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
import connectors.parsers.CreateUpdateEmploymentFinancialDataHttpParser._
import models.api.EmploymentFinancialData
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.DESTaxYearHelper.desTaxYearConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateUpdateEmploymentFinancialDataConnector @Inject()(val http: HttpClientV2,
                                                             val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def createUpdateEmploymentFinancialData(nino: String, taxYear: Int, employmentId: String, employmentFinancialData: EmploymentFinancialData)
                                         (implicit hc: HeaderCarrier): Future[CreateUpdateEmploymentFinancialDataResponse] = {
    val url = url"$baseUrl/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId"

    def call(implicit hc: HeaderCarrier): Future[CreateUpdateEmploymentFinancialDataResponse] =
      http.put(url)
        .withBody(Json.toJson(employmentFinancialData))
        .execute

    call(integrationFrameworkHeaderCarrier(url, CREATE_UPDATE_EMPLOYMENT_DATA))
  }
}
