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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import support.builders.api.EmploymentDataBuilder.anEmploymentData
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}

import scala.concurrent.ExecutionContext.Implicits.global

class GetEmploymentDataConnectorSpec extends ConnectorIntegrationTest {

  private val nino = "some-nino"
  private val employmentId = "some-employment-id"
  private val view = "any-view-value"
  private val taxYear21_22 = 2022
  private val taxYear23_24 = 2024
  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val underTest = new GetEmploymentDataConnector(httpClient, appConfigStub)

  ".getEmploymentData" when {
    "taxYear is 23-24" should {
      "return correct IF data when correct parameters are passed" in {
        val httpResponse = HttpResponse(OK, Json.toJson(anEmploymentData).toString())

        stubGetHttpClientCall(s"/income-tax/income/employments/23-24/$nino/$employmentId\\?view=$view", httpResponse)

        await(underTest.getEmploymentData(nino, taxYear23_24, employmentId, view)(hc)) shouldBe Right(Some(anEmploymentData))
      }

      "return IF error and perform a pagerDutyLog when Left is returned" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(SingleErrorBody("some-code", "some-reason")).toString())

        stubGetHttpClientCall(s"/income-tax/income/employments/23-24/$nino/$employmentId\\?view=$view", httpResponse)

        await(underTest.getEmploymentData(nino, taxYear23_24, employmentId, view)(hc)) shouldBe
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
      }
    }

    "taxYear is not 23-24" should {
      "return correct IF data when correct parameters are passed" in {
        val httpResponse = HttpResponse(OK, Json.toJson(anEmploymentData).toString())

        stubGetHttpClientCall(s"/income-tax/income/employments/$nino/2021-22/$employmentId\\?view=$view", httpResponse)

        await(underTest.getEmploymentData(nino, taxYear21_22, employmentId, view)(hc)) shouldBe Right(Some(anEmploymentData))
      }

      "return IF error and perform a pagerDutyLog when Left is returned" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(SingleErrorBody("some-code", "some-reason")).toString())

        stubGetHttpClientCall(s"/income-tax/income/employments/$nino/2021-22/$employmentId\\?view=$view", httpResponse)

        await(underTest.getEmploymentData(nino, taxYear21_22, employmentId, view)(hc)) shouldBe
          Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
      }
    }
  }
}
