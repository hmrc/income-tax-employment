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
import connectors.parsers.CreateUpdateOtherEmploymentsIncomeHttpParser.{CreateUpdateOtherEmploymentsIncomeHttpReads, CreateUpdateOtherEmploymentsIncomeResponse}
import models.api.OtherEmploymentIncome
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateUpdateOtherEmploymentIncomeConnector @Inject()(val http: HttpClient,
                                                           val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesConnector {
  def createUpdateOtherEmploymentIncome(nino: String,
                                        taxYear: Int,
                                        otherEmploymentIncome: OtherEmploymentIncome)
                                       (implicit hc: HeaderCarrier): Future[CreateUpdateOtherEmploymentsIncomeResponse] = {
    val url = new URL(s"$baseUrl/income-tax/income/other/employments/$nino/${desTaxYearConverter(taxYear)}")

    def desCall(implicit hc: HeaderCarrier): Future[CreateUpdateOtherEmploymentsIncomeResponse] = {
      http.PUT[OtherEmploymentIncome, CreateUpdateOtherEmploymentsIncomeResponse](url, otherEmploymentIncome)
    }

    desCall(desHeaderCarrier(url))
  }
}
