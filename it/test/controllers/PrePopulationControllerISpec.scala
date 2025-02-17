/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import models.api
import models.api.{CustomerEmployment, EmploymentList, HmrcEmployment}
import models.prePopulation.PrePopulationResponse
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.ControllerIntegrationTest
import support.stubs.{AuthStub, WireMockStubs}
import uk.gov.hmrc.http.HttpResponse

class PrePopulationControllerISpec extends ControllerIntegrationTest
  with WireMockStubs
  with AuthStub {

  trait Test {
    val nino: String = "AA111111A"
    val taxYear: Int = 2024
    val mtdItId: String = "1234567890"
    def ifUrl(): String = s"/income-tax/income/employments/$nino/2023-24"

    def request(): WSRequest = {
      authorised()
      buildRequest(s"/income-tax-employment/income-tax/pre-population/$nino/$taxYear")
        .withHttpHeaders(
          (AUTHORIZATION, "Bearer 123"),
          ("mtditid", mtdItId)
        )
    }

    def employmentListAsJson(
                              employments: Option[Seq[HmrcEmployment]],
                              customerDeclaredEmployments: Option[Seq[CustomerEmployment]]
                            ): String =
      Json.toJson(
        EmploymentList(
          employments,
          customerDeclaredEmployments
        )
      ).toString()


    val hmrcEmploymentModel: api.HmrcEmployment =
      api.HmrcEmployment(
        employmentId = "00000000-0000-0000-1111-000000000000",
        employerRef = Some("666/66666"),
        employerName = "Business",
        payrollId = Some("1234567890"),
        startDate = Some("2020-01-01"),
        cessationDate = Some("2020-01-01"),
        occupationalPension = Some(false),
        dateIgnored = Some("2020-01-01T10:00:38Z")
      )


    val customerEmploymentModel: api.CustomerEmployment =
      api.CustomerEmployment(
        employmentId = "00000000-0000-0000-2222-000000000000",
        employerRef = Some("666/66666"),
        employerName = "Business",
        payrollId = Some("1234567890"),
        startDate = Some("2020-01-01"),
        cessationDate = Some("2020-01-01"),
        occupationalPension = Some(false),
        submittedOn = "2020-01-01T10:00:38Z"
      )
  }

  "/pre-population/:nino/:taxYear" when {
    val notFoundHttpResponse: HttpResponse = HttpResponse(NOT_FOUND, "no teapot found")

    "IFS returns a non-404 error when retrieving a user's Data employment" should {
      "return an INTERNAL SERVER ERROR response" in new Test {
        stubGetHttpClientCall(ifUrl(), HttpResponse(IM_A_TEAPOT, "teapot time"))

        val result: WSResponse = await(request().get())
        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "IFS returns a 404 error when retrieving a user's Data employment" should {
      "return an empty pre-pop response" in new Test {
        stubGetHttpClientCall(ifUrl(), notFoundHttpResponse)

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.noPrePop)
      }
    }

    "IFS returns empty response when retrieving a user's Data employment" should {
      "return an empty pre-pop response" in new Test {
        stubGetHttpClientCall(ifUrl(), HttpResponse(OK, employmentListAsJson(None,None)))

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.noPrePop)
      }
    }

    "IFS returns response of dateignored hmrc record  when retrieving a user's Data employment" should {
      "return an empty pre-pop response" in new Test {

        val hmrcEmployment = Option(
          Seq(
            hmrcEmploymentModel,
            hmrcEmploymentModel.copy(startDate = Some("2020-02-01") )
          )
        )
        stubGetHttpClientCall(ifUrl(), HttpResponse(OK, employmentListAsJson(hmrcEmployment, None)))

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.noPrePop)
      }
    }

    "IFS returns response of at least one hmrc dateignored is None  when retrieving a user's Data employment" should {
      "return none empty pre-pop response" in new Test {

        val hmrcEmployment = Option(
          Seq(
            hmrcEmploymentModel,
            hmrcEmploymentModel.copy(startDate = Some("2020-02-01"), dateIgnored = None )
          )
        )
        stubGetHttpClientCall(ifUrl(), HttpResponse(OK, employmentListAsJson(hmrcEmployment, None)))

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.hasPrePop)
      }
    }

    "IFS returns response of none empty CustomerEmployment of  a user's Data employment" should {
      "return none empty pre-pop response" in new Test {

        val hmrcEmployment = Option(
          Seq(
            hmrcEmploymentModel,
            hmrcEmploymentModel.copy(startDate = Some("2020-02-01") )
          )
        )
        val cust =  Option(
          Seq(
            customerEmploymentModel
          )
        )
        stubGetHttpClientCall(ifUrl(), HttpResponse(OK, employmentListAsJson(hmrcEmployment, cust)))

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.hasPrePop)
      }
    }

    "IFS returns response of none empty CustomerEmployment and empty hmrc data of a user's Data employment" should {
      "return none empty pre-pop response" in new Test {

        val cust =  Option(
          Seq(
            customerEmploymentModel
          )
        )
        stubGetHttpClientCall(ifUrl(), HttpResponse(OK, employmentListAsJson(None, cust)))

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.hasPrePop)
      }
    }

    "IFS returns response of none empty multiple CustomerEmployment and empty hmrc data of a user's Data employment" should {
      "return none empty pre-pop response" in new Test {

        val cust =  Option(
          Seq(
            customerEmploymentModel,
            customerEmploymentModel.copy(employerName = "Some Business")
          )
        )
        stubGetHttpClientCall(ifUrl(), HttpResponse(OK, employmentListAsJson(None, cust)))

        val result: WSResponse = await(request().get())
        result.status shouldBe OK
        result.json shouldBe Json.toJson(PrePopulationResponse.hasPrePop)
      }
    }
  }
}
