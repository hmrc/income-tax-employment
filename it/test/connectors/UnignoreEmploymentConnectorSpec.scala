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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter
import utils.TaxYearUtils.toTaxYearParam

import scala.concurrent.ExecutionContext.Implicits.global

class UnignoreEmploymentConnectorSpec extends ConnectorIntegrationTest {

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
  private val connector = new UnignoreEmploymentConnector(httpClientV2, appConfigStub)
  private val nino = "AA123456B"
  private val employmentId = "436245634563456436456"
  private val headersSentToIntegrationFramework: Seq[HttpHeader] = Seq(
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  ".UnignoreEmploymentConnector before 23-24" should {
    val taxYear = 2022
    val url = s"/income-tax/employments/$nino/${desTaxYearConverter(taxYear)}/ignore/$employmentId"

      "the host for IF is 'Internal'" in {
        val httpResponse = HttpResponse(NO_CONTENT, "{}")
        stubDeleteHttpClientCall(url, httpResponse, headersSentToIntegrationFramework)

        val result = await(connector.unignoreEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Right(())
    }

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, FORBIDDEN, UNPROCESSABLE_ENTITY, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"Integration Framework returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)
          val httpResponse = HttpResponse(status, desError.toJson.toString())
          stubDeleteHttpClientCall(url, httpResponse, headersSentToIntegrationFramework)

          val result = await(connector.unignoreEmployment(nino, taxYear, employmentId)(hc))
          result mustBe Left(desError)
        }
      }

      s"Integration Framework returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        val httpResponse = HttpResponse(BAD_GATEWAY, desError.toJson.toString())
        stubDeleteHttpClientCall(url, httpResponse)

        val result = await(connector.unignoreEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Left(desError)
      }
    }
  }
  ".UnignoreEmploymentConnector after 23-24" should {

    val taxYear = 2024
    val url = s"/income-tax/${toTaxYearParam(taxYear)}/employments/$nino/ignore/$employmentId"
    "return successful response" in {

      val httpResponse = HttpResponse(NO_CONTENT, "{}")
      stubDeleteHttpClientCall(url, httpResponse, headersSentToIntegrationFramework)

      val result = await(connector.unignoreEmployment(nino, taxYear, employmentId)(hc))

      result mustBe Right(())
    }

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, FORBIDDEN, UNPROCESSABLE_ENTITY, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"Integration Framework returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)
          val httpResponse = HttpResponse(status, desError.toJson.toString())
          stubDeleteHttpClientCall(url, httpResponse, headersSentToIntegrationFramework)

          val result = await(connector.unignoreEmployment(nino, taxYear, employmentId)(hc))
          result mustBe Left(desError)
        }
      }

      s"Integration Framework returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)

        val httpResponse = HttpResponse(BAD_GATEWAY, desError.toJson.toString())
        stubDeleteHttpClientCall(url, httpResponse, headersSentToIntegrationFramework)


        val result = await(connector.unignoreEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Left(desError)
      }
    }
  }
}
