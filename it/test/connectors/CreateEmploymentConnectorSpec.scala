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
import models.shared.{AddEmploymentResponseModel, CreateUpdateEmployment}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class CreateEmploymentConnectorSpec extends ConnectorIntegrationTest {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
  private val connector = new CreateEmploymentConnector(httpClientV2, appConfigStub)

  val taxYear = 2022

  ".CreateEmploymentConnector" should {
    val nino = "taxable_entity_id"

    val addEmploymentModel = CreateUpdateEmployment(Some("employerRef"), "employerName", LocalDateTime.now().toString, Some(LocalDateTime.now().toString), Some("payrollId"))

    val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/custom"

    "include internal headers" when {
      val expectedResult = AddEmploymentResponseModel("employmendId")
      val responseBody = Json.toJson(expectedResult).toString()
      val requestBody = Json.toJson(addEmploymentModel).toString()

      val headersSentToIntegrationFramework = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      "the host for Integration Framework is 'Internal'" in {
        val httpResponse = HttpResponse(OK, responseBody)
        stubPostHttpClientCall(url, requestBody, httpResponse, headersSentToIntegrationFramework)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for Integration Framework is 'External'" in {

        val httpResponse = HttpResponse(OK, responseBody)
        stubPostHttpClientCall(url, requestBody, httpResponse, headersSentToIntegrationFramework)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return error" when {
      val expectedResult = Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError()))

      "when Integration Framework returns 200 but the schema of the json response body is unexpected" in {

        val httpResponse = HttpResponse(OK, "{}")
        stubPostHttpClientCall(url, Json.toJson(addEmploymentModel).toString(), httpResponse)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe expectedResult
      }
    }

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      val requestBody = Json.toJson(addEmploymentModel).toString()

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"DES returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)

          val httpResponse = HttpResponse(status, desError.toJson.toString())
          stubPostHttpClientCall(url, Json.toJson(addEmploymentModel).toString(), httpResponse)

          val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel))

          result mustBe Left(desError)
        }
      }

      s"Integration Framework returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)

        val httpResponse = HttpResponse(BAD_GATEWAY, desError.toJson.toString)
        stubPostHttpClientCall(url, Json.toJson(addEmploymentModel).toString(), httpResponse)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel))

        result mustBe Left(desError)
      }

    }
  }
}
