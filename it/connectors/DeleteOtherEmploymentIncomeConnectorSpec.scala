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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter

import scala.concurrent.ExecutionContext.Implicits.global

class DeleteOtherEmploymentIncomeConnectorSpec extends ConnectorIntegrationTest
  with TaxYearProvider {

  private val nino = "some-nino"

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val underTest = new DeleteOtherEmploymentIncomeConnector(httpClient, appConfigStub)

  ".deleteOtherEmploymentIncome" should {
    "return a successful response when the call is successful" in {
      val httpResponse = HttpResponse(NO_CONTENT, "")

      stubDeleteHttpClientCall(s"/income-tax/income/other/employments/$nino/${desTaxYearConverter(taxYear)}", httpResponse)

      await(underTest.deleteEmployment(nino, taxYear)(hc)) shouldBe Right()
    }

    "return an error response when the call to the backend returns an error" in {
      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(SingleErrorBody("some-code", "some-reason")).toString())

      stubDeleteHttpClientCall(s"/income-tax/income/other/employments/$nino/${desTaxYearConverter(taxYear)}", httpResponse)

      await(underTest.deleteEmployment(nino, taxYear)(hc)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
    }
  }
}
