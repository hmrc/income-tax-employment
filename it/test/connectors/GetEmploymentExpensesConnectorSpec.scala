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

import connectors.GetEmploymentExpensesConnectorSpec.expectedResponseBody
import connectors.errors.{ApiError, SingleErrorBody}
import models.api.EmploymentExpenses
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}

import scala.concurrent.ExecutionContext.Implicits.global

class GetEmploymentExpensesConnectorSpec extends ConnectorIntegrationTest {

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val connector = new GetEmploymentExpensesConnector(httpClientV2, appConfigStub)

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val employmentId: String = "00000000-0000-1000-8000-000000000000"
  val view: String = "CUSTOMER"
  val getEmploymentDataUrl = s"/income-tax-expenses/income-tax/nino/$nino/sources\\?view=$view&taxYear=$taxYear"

  ".GetEmploymentExpensesConnector" should {
    "return a DESEmploymentExpenses" when {
      "all values are present in the url" in {
        val expectedResult = Json.parse(expectedResponseBody).as[EmploymentExpenses]

        val httpResponse = HttpResponse(OK, expectedResponseBody)

        stubGetHttpClientCall(getEmploymentDataUrl, httpResponse)

        val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

        result.toOption.get.get.dateIgnored mustBe expectedResult.dateIgnored
        result.toOption.get.get.expenses mustBe expectedResult.expenses
        result.toOption.get.get.totalExpenses mustBe expectedResult.totalExpenses
        result.toOption.get.get.source mustBe expectedResult.source
        result.toOption.get.get.submittedOn mustBe expectedResult.submittedOn
      }
    }

    "return a right none as all fields are optional" in {
      val invalidJson = Json.obj(
        "employments" -> ""
      )
      val httpResponse = HttpResponse(OK, invalidJson.toString())

      stubGetHttpClientCall(getEmploymentDataUrl, httpResponse)

      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Right(None)
    }

    "return INTERNAL_SERVER_ERROR when downstream returns no data" in {
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError(false))

      val httpResponse = HttpResponse(NO_CONTENT, "{}")
      stubGetHttpClientCall(getEmploymentDataUrl, httpResponse)
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
    }

    Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
      s"return a $status" in {
        val responseBody = Json.obj(
          "code" -> "DES_CODE",
          "reason" -> "DES_REASON"
        )
        val httpResponse = HttpResponse(status, responseBody.toString())
        stubGetHttpClientCall(getEmploymentDataUrl, httpResponse)

        val expectedResult = ApiError(status, SingleErrorBody("DES_CODE", "DES_REASON"))

        val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

        result mustBe Left(expectedResult)
      }
    }

    "return a Right None" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )
      val httpResponse = HttpResponse(NOT_FOUND, responseBody.toString())
      stubGetHttpClientCall(getEmploymentDataUrl, httpResponse)

      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Right(None)
    }

    Seq(CONFLICT, FORBIDDEN).foreach { status =>
      s"return an Internal Server Error when DES throws an unexpected result and status $status" in {
        val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError(false))
        val httpResponse = HttpResponse(status, SingleErrorBody.parsingError(false).toString)
        stubGetHttpClientCall(getEmploymentDataUrl, httpResponse)

        val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))
        result mustBe Left(expectedResult)
      }
    }
  }
}

object GetEmploymentExpensesConnectorSpec {
  val expectedResponseBody: String =
    """{
      |	"submittedOn": "2020-01-04T05:01:01Z",
      |	"dateIgnored": "2020-01-04T05:01:01Z",
      |	"source": "HMRC-HELD",
      |	"totalExpenses": 800,
      |	"expenses": {
      |		"businessTravelCosts": 100,
      |		"jobExpenses": 100,
      |		"flatRateJobExpenses": 100,
      |		"professionalSubscriptions": 100,
      |		"hotelAndMealExpenses": 100,
      |		"otherAndCapitalAllowances": 100,
      |		"vehicleExpenses": 100,
      |		"mileageAllowanceRelief": 100
      |	}
      |}""".stripMargin
}