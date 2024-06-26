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
import connectors.parsers.IgnoreEmploymentHttpParser.{IgnoreEmploymentHttpReads, IgnoreEmploymentResponse}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter
import utils.TaxYearUtils.{isAfter2324Api, toTaxYearParam}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IgnoreEmploymentConnector @Inject()(val http: HttpClient,
                                          val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  def ignoreEmployment(nino: String, taxYear: Int, employmentId: String)
                      (implicit hc: HeaderCarrier): Future[IgnoreEmploymentResponse] = {

    val (url, apiVersion) = if (isAfter2324Api(taxYear)) {
      (new URL(s"$baseUrl/income-tax/${toTaxYearParam(taxYear)}/income/employments/$nino/$employmentId/ignore"), IGNORE_EMPLOYMENT_23_34)
    } else {
      (new URL(s"$baseUrl/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId/ignore"), IGNORE_EMPLOYMENT)
    }

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[IgnoreEmploymentResponse] = {
      http.PUT[JsValue, IgnoreEmploymentResponse](url, Json.parse("""{}"""))
    }

    integrationFrameworkCall(integrationFrameworkHeaderCarrier(url, apiVersion))
  }
}
