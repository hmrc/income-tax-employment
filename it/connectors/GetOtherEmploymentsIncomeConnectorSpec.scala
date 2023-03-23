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
import support.builders.api.OtherEmploymentsIncomeBuilder.anOtherEmploymentsIncome
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter

import scala.concurrent.ExecutionContext.Implicits.global

class GetOtherEmploymentsIncomeConnectorSpec extends ConnectorIntegrationTest
  with TaxYearProvider {

  private val nino = "some-nino"

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  private val underTest = new GetOtherEmploymentIncomeConnector(httpClient, appConfigStub)

  ".getOtherEmploymentsIncome" should {
    "return correct data when correct parameters are passed" in {
      val httpResponse = HttpResponse(OK, Json.toJson(anOtherEmploymentsIncome).toString())

      stubGetHttpClientCall(s"/income-tax/income/other/employments/$nino/${desTaxYearConverter(taxYear)}", httpResponse)

      await(underTest.getOtherEmploymentIncome(nino, taxYear)(hc)) shouldBe Right(Some(anOtherEmploymentsIncome))
    }

    "return error and perform a pagerDutyLog when Left is returned" in {
      val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Json.toJson(SingleErrorBody("some-code", "some-reason")).toString())

      stubGetHttpClientCall(s"/income-tax/income/other/employments/$nino/${desTaxYearConverter(taxYear)}", httpResponse)

      await(underTest.getOtherEmploymentIncome(nino, taxYear)(hc)) shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("some-code", "some-reason")))
    }
  }
}
