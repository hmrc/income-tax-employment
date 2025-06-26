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

import com.github.tomakehurst.wiremock.http.HttpHeader
import connectors.errors.{ApiError, SingleErrorBody}
import models._
import models.api.{Employment, PayModel}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, SessionId}
import utils.TaxYearUtils.toTaxYearParam

import scala.concurrent.ExecutionContext.Implicits.global

class CreateUpdateEmploymentFinancialDataTYSConnectorSpec extends ConnectorIntegrationTest {


  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
  private val connector = new CreateUpdateEmploymentFinancialDataTYSConnector(httpClientV2, appConfigStub)

  val nino: String = "123456789"
  val specificTaxYear: Int = 2024
  val employmentId: String = "0000000-0000000-000000"
  val minEmployment: Employment = Employment(pay = PayModel(taxablePayToDate = 100.00, totalTaxToDate = 100.00, tipsAndOtherPayments = None), None, None, Some(true))
  val minEmploymentFinancialData: api.EmploymentFinancialData = api.EmploymentFinancialData(minEmployment)
  val stubUrl = s"/income-tax/${toTaxYearParam(specificTaxYear)}/income/employments/$nino/$employmentId"

  "CreateUpdateEmploymentFinancialDataTYSConnector" should {

    val headersToSend = Seq(
      new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
    )
    "return a success result and include the correct headers" when {

      "Integration Framework Returns a 204 with minimum data sent in the body" in {

        val httpResponse = HttpResponse(NO_CONTENT, "")
        stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse, headersToSend)

        val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }
    }

    Seq(
      ("NotFound", NOT_FOUND),
      ("BadRequest", BAD_REQUEST),
      ("Server Error", INTERNAL_SERVER_ERROR),
      ("The remote endpoint has indicated that this tax year is not supported.", UNPROCESSABLE_ENTITY),
      ("Server unavailable", SERVICE_UNAVAILABLE)
    ) foreach { case (reason, status) =>
      s"returns a $status response" in {
        val expectedResult = ApiError(status, SingleErrorBody("INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear."))

        val responseBody = Json.obj(
          "code" -> "INVALID_TAX_YEAR",
          "reason" -> "Submission has not passed validation. Invalid parameter taxYear."
        )
        val httpResponse = HttpResponse(status, responseBody.toString())
        stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse, headersToSend)

        val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Left(expectedResult)
      }
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable"))
      val httpResponse = HttpResponse(CONFLICT, responseBody.toString())
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse, headersToSend)
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }
  }
}
