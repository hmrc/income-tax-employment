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
import play.api.libs.json.{JsObject, Json}
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter

import scala.concurrent.ExecutionContext.Implicits.global

class CreateUpdateEmploymentFinancialDataConnectorSpec extends ConnectorIntegrationTest {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
  private val connector = new CreateUpdateEmploymentFinancialDataConnector(httpClientV2, appConfigStub)

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val employmentId: String = "0000000-0000000-000000"
  val minEmployment: Employment = Employment(pay = PayModel(taxablePayToDate = 100.00, totalTaxToDate = 100.00, tipsAndOtherPayments = None), None, None, None)
  val minEmploymentFinancialData: api.EmploymentFinancialData = api.EmploymentFinancialData(minEmployment)
  val stubUrl = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId"

  val headersToSend: Seq[HttpHeader] = Seq(
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  "PutEmploymentFinancialDataConnector" should {

    "return a success result and include the correct headers" when {

      "Integration Framework Returns a 204 with minimum data sent in the body" in {

        val httpResponse = HttpResponse(NO_CONTENT, "")

        stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse, headersToSend)

        val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }

      "Integration Framework Returns a 204 with maximum data sent in the body" in {
        val httpResponse = HttpResponse(NO_CONTENT, "")
        stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)

        val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }
    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj(
        "financialData" -> ""
      )

      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError())

      val httpResponse = HttpResponse(OK, invalidJson.toString())
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Forbidden" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )
      val expectedResult = ApiError(FORBIDDEN, SingleErrorBody("NOT_FOUND_INCOME_SOURCE", "Can't find income source"))
      val httpResponse = HttpResponse(FORBIDDEN, responseBody.toString())
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)

      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Internal Server Error" in {
      val responseBody: JsObject = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal server error"
      )
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVER_ERROR", "Internal server error"))

      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, responseBody.toString())
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable"))
      val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, responseBody.toString())
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result with no body" in {
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError())
      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, "")
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable"))
      val httpResponse = HttpResponse(CONFLICT, responseBody.toString())
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError())
      val httpResponse = HttpResponse(CONFLICT, responseBody.toString())
      stubPutHttpClientCall(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), httpResponse)

      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }
  }
}
