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

import connectors.errors.{ApiError, SingleErrorBody}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter
import utils.TaxYearUtils.toTaxYearParam

import scala.concurrent.ExecutionContext.Implicits.global

class DeleteEmploymentFinancialDataConnectorSpec extends ConnectorIntegrationTest {

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val connector = new DeleteEmploymentFinancialDataConnector(httpClientV2, appConfigStub)

  val nino = "taxable_entity_id"
  val employmentId = "employment_id"

  ".DeleteEmploymentFinancialDataConnector before 2023" should {
    val taxYear = 2022
    val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId"

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"DES returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)

          val httpResponse = HttpResponse(status, desError.toJson.toString())

          stubDeleteHttpClientCall(url, httpResponse)

          val result = await(connector.deleteEmploymentFinancialData(nino, taxYear, employmentId)(hc))

          result mustBe Left(desError)
        }
      }

      s"IF returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)

        val httpResponse = HttpResponse(BAD_GATEWAY, desError.toJson.toString())
        stubDeleteHttpClientCall(url, httpResponse)

        val result = await(connector.deleteEmploymentFinancialData(nino, taxYear, employmentId)(hc))

        result mustBe Left(desError)
      }

    }
  }
  ".DeleteEmploymentFinancialDataConnector After 2024" should {
    val taxYear = 2024
    val url = s"/income-tax/${toTaxYearParam(taxYear)}/income/employments/$nino/$employmentId"

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"DES returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          val httpResponse = HttpResponse(status, desError.toJson.toString())
          stubDeleteHttpClientCall(url, httpResponse)

          val result = await(connector.deleteEmploymentFinancialData(nino, taxYear, employmentId))

          result mustBe Left(desError)
        }
      }

      s"IF returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        val httpResponse = HttpResponse(BAD_GATEWAY, desError.toJson.toString())
        stubDeleteHttpClientCall(url, httpResponse)

        val result = await(connector.deleteEmploymentFinancialData(nino, taxYear, employmentId))

        result mustBe Left(desError)
      }

    }
  }
}
