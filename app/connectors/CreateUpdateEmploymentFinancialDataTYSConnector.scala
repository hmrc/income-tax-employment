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
import connectors.parsers.CreateUpdateEmploymentFinancialDataTYSHttpParser._
import models.api.EmploymentFinancialData
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import utils.TaxYearUtils._

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateUpdateEmploymentFinancialDataTYSConnector @Inject()(val http: HttpClientV2,
                                                                val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def createUpdateEmploymentFinancialData(nino: String, taxYear: Int, employmentId: String, employmentFinancialData: EmploymentFinancialData)
                                         (implicit hc: HeaderCarrier): Future[CreateUpdateEmploymentFinancialDataTYSResponse] = {
    val url: URL = url"$baseUrl/income-tax/${toTaxYearParam(taxYear)}/income/employments/$nino/$employmentId"

    def call(implicit hc: HeaderCarrier): Future[CreateUpdateEmploymentFinancialDataTYSResponse] =
      http.put(url)
        .withBody(Json.toJson(employmentFinancialData))
        .execute

    call(integrationFrameworkHeaderCarrier(url, CREATE_UPDATE_EMPLOYMENT_DATA_23_24))
  }
}
