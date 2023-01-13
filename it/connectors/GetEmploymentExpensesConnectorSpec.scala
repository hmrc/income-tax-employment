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
import config.BackendAppConfig
import connectors.GetEmploymentExpensesConnectorSpec.expectedResponseBody
import helpers.WiremockSpec
import models.DES.DESEmploymentExpenses
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class GetEmploymentExpensesConnectorSpec extends PlaySpec with WiremockSpec {

  lazy val connector: GetEmploymentExpensesConnector = app.injector.instanceOf[GetEmploymentExpensesConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  private def appConfig(expensesHost: String) =
    new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
      override val expensesBaseUrl: String = s"http://$expensesHost:$wireMockPort"
    }

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val employmentId: String = "00000000-0000-1000-8000-000000000000"
  val view: String = "CUSTOMER"
  val getEmploymentDataUrl = s"/income-tax-expenses/income-tax/nino/$nino/sources\\?view=$view&taxYear=$taxYear"

  ".GetEmploymentExpensesConnector" should {
    "include internal headers" when {
      val expectedResult = Some(Json.parse(expectedResponseBody).as[DESEmploymentExpenses])

      val headersSentToExpenses = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for Expenses is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetEmploymentExpensesConnector(httpClient, appConfig(internalHost))

        stubGetWithResponseBody(getEmploymentDataUrl, OK, expectedResponseBody, headersSentToExpenses)

        val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for Expenses is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetEmploymentExpensesConnector(httpClient, appConfig(externalHost))

        stubGetWithResponseBody(getEmploymentDataUrl, OK, expectedResponseBody, headersSentToExpenses)

        val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return a DESEmploymentExpenses" when {
      "all values are present in the url" in {
        val expectedResult = Json.parse(expectedResponseBody).as[DESEmploymentExpenses]
        stubGetWithResponseBody(getEmploymentDataUrl, OK, expectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
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

      stubGetWithResponseBody(getEmploymentDataUrl, OK, invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Right(None)
    }

    "return a NO_CONTENT" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError(false))

      stubGetWithResponseBody(getEmploymentDataUrl, NO_CONTENT, "{}")
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Bad Request" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

      stubGetWithResponseBody(getEmploymentDataUrl, BAD_REQUEST, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Right None" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )

      stubGetWithResponseBody(getEmploymentDataUrl, NOT_FOUND, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Right(None)
    }

    "return an Internal server error" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal server error"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("SERVER_ERROR", "Internal server error"))

      stubGetWithResponseBody(getEmploymentDataUrl, INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(getEmploymentDataUrl, SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError(false))

      stubGetWithoutResponseBody(getEmploymentDataUrl, NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(getEmploymentDataUrl, CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError(false))

      stubGetWithResponseBody(getEmploymentDataUrl, CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentExpenses(nino, taxYear, view)(hc))

      result mustBe Left(expectedResult)
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