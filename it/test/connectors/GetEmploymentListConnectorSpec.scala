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

import connectors.GetEmploymentListConnectorSpec.{customerExpectedResponseBody, expectedResponseBody, filteredExpectedResponseBody, hmrcExpectedResponseBody}
import connectors.errors.{ApiError, SingleErrorBody}
import models.api.EmploymentList
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter

import scala.concurrent.ExecutionContext.Implicits.global

class GetEmploymentListConnectorSpec extends ConnectorIntegrationTest {

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val connector = new GetEmploymentListConnector(httpClientV2, appConfigStub)
  val nino: String = "123456789"
  val taxYear: Int = 1999
  val employmentId: String = "00000000-0000-1000-8000-000000000000"
  val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}"

  ".GetEmploymentListConnector" should {
    "return a GetEmploymentListModel" when {
      "only nino and taxYear are present" in {
        val expectedResult = Json.parse(expectedResponseBody).as[EmploymentList]
        val httpResponse = HttpResponse(OK, expectedResponseBody)

        stubGetHttpClientCall(url, httpResponse)
        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc)).toOption.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }

      "when customer is empty" in {
        val expectedResult = Json.parse(hmrcExpectedResponseBody).as[EmploymentList]

        val httpResponse = HttpResponse(OK, hmrcExpectedResponseBody)

        stubGetHttpClientCall(url, httpResponse)
        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc)).toOption.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }

      "when hmrc is empty" in {
        val expectedResult = Json.parse(customerExpectedResponseBody).as[EmploymentList]
        val httpResponse = HttpResponse(OK, customerExpectedResponseBody)
        stubGetHttpClientCall(url, httpResponse)
        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc)).toOption.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }

      "nino, taxYear and employmentId are present" in {
        val expectedResult = Json.parse(filteredExpectedResponseBody).as[EmploymentList]
        val httpResponse = HttpResponse(OK, filteredExpectedResponseBody)
        stubGetHttpClientCall(s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}\\?employmentId=$employmentId", httpResponse)
        val result = await(connector.getEmploymentList(nino, taxYear, Some(employmentId))(hc)).toOption.get.get

        result.employments mustBe expectedResult.employments
        result.customerDeclaredEmployments mustBe expectedResult.customerDeclaredEmployments
      }
    }


    Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
      s"return a $status" in {
        val responseBody = Json.obj(
          "code" -> "DES_CODE",
          "reason" -> "DES_REASON"
        )
        val httpResponse = HttpResponse(status, responseBody.toString())
        stubGetHttpClientCall(url, httpResponse)

        val expectedResult = ApiError(status, SingleErrorBody("DES_CODE", "DES_REASON"))

        val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

        result mustBe Left(expectedResult)
      }
    }

    "return a NO_CONTENT" in {
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError())
      val httpResponse = HttpResponse(NO_CONTENT, "{}")
      stubGetHttpClientCall(url, httpResponse)
      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Right None" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )

      val httpResponse = HttpResponse(NOT_FOUND, responseBody.toString())
      stubGetHttpClientCall(url, httpResponse)

      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Right(None)
    }

    "return a Right None when both empty" in {
      val expectedResult = Json.parse("""{}""")
      val httpResponse = HttpResponse(NOT_FOUND, expectedResult.toString())
      stubGetHttpClientCall(url, httpResponse)

      val result = await(connector.getEmploymentList(nino, taxYear, None)(hc))

      result mustBe Right(None)
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