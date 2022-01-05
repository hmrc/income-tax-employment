/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.GetEmploymentListConnectorSpec.{customerExpectedResponseBody, expectedResponseBody, filteredExpectedResponseBody, hmrcExpectedResponseBody}
import helpers.WiremockSpec
import models.DES.DESEmploymentList
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter

class GetEmploymentListConnectorSpec extends PlaySpec with WiremockSpec {

  lazy val connector: GetEmploymentListConnector = app.injector.instanceOf[GetEmploymentListConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  def appConfig(integrationFrameworkHost: String): BackendAppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val integrationFrameworkBaseUrl: String = s"http://$integrationFrameworkHost:$wireMockPort"
  }

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val employmentId: String = "00000000-0000-1000-8000-000000000000"

  ".GetEmploymentListConnector" should {
    "include internal headers" when {
      val expectedResult = Some(Json.parse(expectedResponseBody).as[DESEmploymentList])

      val headersSentToIntegrationFramework = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for DES is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetEmploymentListConnector(httpClient, appConfig(internalHost))

        stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", OK, expectedResponseBody, headersSentToIntegrationFramework)

        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for Integration Framework is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetEmploymentListConnector(httpClient, appConfig(externalHost))

        stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", OK, expectedResponseBody, headersSentToIntegrationFramework)

        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return a GetEmploymentListModel" when {
      "only nino and taxYear are present" in {
        val expectedResult = Json.parse(expectedResponseBody).as[DESEmploymentList]
        stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", OK, expectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc)).right.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }

      "when customer is empty" in {
        val expectedResult = Json.parse(hmrcExpectedResponseBody).as[DESEmploymentList]
        stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", OK, hmrcExpectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc)).right.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }

      "when hmrc is empty" in {
        val expectedResult = Json.parse(customerExpectedResponseBody).as[DESEmploymentList]
        stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", OK, customerExpectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc)).right.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }

      "nino, taxYear and employmentId are present" in {
        val expectedResult = Json.parse(filteredExpectedResponseBody).as[DESEmploymentList]

        stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}\\?employmentId=$employmentId",
          OK, filteredExpectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getEmploymentList(nino, taxYear, Some(employmentId))(hc)).right.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }
    }


    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj(
        "employments" -> ""
      )

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError())

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", OK, invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return a NO_CONTENT" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError())

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", NO_CONTENT, "{}")
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Bad Request" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", BAD_REQUEST, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Right None" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", NOT_FOUND, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Right(None)
    }
    "return a Right None when both empty" in {
      val expectedResult = Json.parse("""{}""")

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", OK, expectedResult.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Right(None)
    }

    "return an Internal server error" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal server error"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("SERVER_ERROR", "Internal server error"))

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError())

      stubGetWithoutResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel.parsingError())

      stubGetWithResponseBody(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}", CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }
  }
}

object GetEmploymentListConnectorSpec {
  val expectedResponseBody: String =
    """
      |{
      |	"employments": [
      |		{
      |			"employmentId": "00000000-0000-1000-8000-000000000000",
      |			"employerName": "Vera Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123345657",
      |			"startDate": "2020-06-17",
      |			"cessationDate": "2020-06-17",
      |			"dateIgnored": "2020-06-17T10:53:38Z"
      |		},
      |  	{
      |			"employmentId": "00000000-0000-2000-8000-000000000000",
      |			"employerName": "Jackie Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123145657",
      |			"startDate": "2020-06-18",
      |			"cessationDate": "2020-06-17",
      |			"dateIgnored": "2020-06-17T10:53:38Z"
      |		}
      |	],
      |	"customerDeclaredEmployments": [
      |		{
      |			"employmentId": "00000000-0000-1000-8000-000000000003",
      |			"employerName": "Vera Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123345657",
      |			"startDate": "2020-06-17",
      |			"cessationDate": "2020-06-17",
      |			"submittedOn": "2020-06-17T10:53:38Z"
      |		},
      |  	{
      |			"employmentId": "00000000-0000-2000-8000-000000000003",
      |			"employerName": "Jackie Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123145657",
      |			"startDate": "2020-06-18",
      |			"cessationDate": "2020-06-17",
      |			"submittedOn": "2020-06-17T10:53:38Z"
      |		}
      |	]
      |}
      |
      |""".stripMargin
  val hmrcExpectedResponseBody: String =
    """
      |{
      |	"employments": [
      |		{
      |			"employmentId": "00000000-0000-1000-8000-000000000000",
      |			"employerName": "Vera Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123345657",
      |			"startDate": "2020-06-17",
      |			"cessationDate": "2020-06-17",
      |			"dateIgnored": "2020-06-17T10:53:38Z"
      |		},
      |  	{
      |			"employmentId": "00000000-0000-2000-8000-000000000000",
      |			"employerName": "Jackie Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123145657",
      |			"startDate": "2020-06-18",
      |			"cessationDate": "2020-06-17",
      |			"dateIgnored": "2020-06-17T10:53:38Z"
      |		}
      |	]
      |}
      |
      |""".stripMargin
  val customerExpectedResponseBody: String =
    """
      |{
      |	"customerDeclaredEmployments": [
      |		{
      |			"employmentId": "00000000-0000-1000-8000-000000000003",
      |			"employerName": "Vera Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123345657",
      |			"startDate": "2020-06-17",
      |			"cessationDate": "2020-06-17",
      |			"submittedOn": "2020-06-17T10:53:38Z"
      |		},
      |  	{
      |			"employmentId": "00000000-0000-2000-8000-000000000003",
      |			"employerName": "Jackie Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123145657",
      |			"startDate": "2020-06-18",
      |			"cessationDate": "2020-06-17",
      |			"submittedOn": "2020-06-17T10:53:38Z"
      |		}
      |	]
      |}
      |
      |""".stripMargin

  val filteredExpectedResponseBody: String =
    """
      |{
      |	"employments": [
      |		{
      |			"employmentId": "00000000-0000-1000-8000-000000000000",
      |			"employerName": "Vera Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123345657",
      |			"startDate": "2020-06-17",
      |			"cessationDate": "2020-06-17",
      |			"dateIgnored": "2020-06-17T10:53:38Z"
      |		}
      |	],
      |	"customerDeclaredEmployments": [
      |		{
      |			"employmentId": "00000000-0000-1000-8000-000000000000",
      |			"employerName": "Vera Lynn",
      |			"employerRef": "123/abc 001<Q>",
      |			"payrollId": "123345657",
      |			"startDate": "2020-06-17",
      |			"cessationDate": "2020-06-17",
      |			"submittedOn": "2020-06-17T10:53:38Z"
      |		}
      |	]
      |}
      |
      |""".stripMargin
}