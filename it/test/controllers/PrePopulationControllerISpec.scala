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

import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.ControllerIntegrationTest
import support.stubs.{AuthStub, WireMockStubs}
import uk.gov.hmrc.http.HttpResponse
import utils.ViewParameterValidation._

class PrePopulationControllerISpec extends ControllerIntegrationTest
  with WireMockStubs
  with AuthStub {

  trait Test {
    val nino: String = "AA111111A"
    val taxYear: Int = 2024
    val mtdItId: String = "1234567890"
    val ifTaxYearParam = s"${(taxYear - 1).toString.takeRight(2)}-${taxYear.toString.takeRight(2)}"

    def ifUrl(): String = s"/income-tax/income/employments/$nino/${ifTaxYearParam}"

    def request(): WSRequest = {
      authorised()
      buildRequest(s"/income-tax-employment/income-tax/pre-population/$nino/$taxYear")
        .withHttpHeaders(
          (AUTHORIZATION, "Bearer 123"),
          ("mtditid", mtdItId)
        )
    }
  }

  "/pre-population/:nino/:taxYear" when {
    "IF returns a non-404 error when retrieving a user's Data employment" should {
      "return an INTERNAL SERVER ERROR response" in new Test {

        stubGetHttpClientCall(
          ifUrl(),
          HttpResponse(IM_A_TEAPOT, "teapot time")
        )

        val result: WSResponse = await(request().get())
        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
