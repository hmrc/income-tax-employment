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
import models.shared.CreateUpdateEmployment
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, SessionId}
import utils.DESTaxYearHelper.desTaxYearConverter

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class UpdateEmploymentConnectorSpec extends ConnectorIntegrationTest {

  private val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
  private val connector = new UpdateEmploymentConnector(httpClientV2, appConfigStub)
  private val taxYear = 2022

  ".UpdateEmploymentConnector" should {

    val nino = "taxable_entity_id"
    val employmentId = "employment_id"
    val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/custom/$employmentId"
    val updateEmploymentModel = CreateUpdateEmployment(Some("employerRef"), "employerName", LocalDateTime.now().toString, Some(LocalDateTime.now().toString), Some("payrollId"))
    val headersSentToIntegrationFramework = Seq(
      new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
    )

    "return successful response" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
      val httpResponse = HttpResponse(NO_CONTENT, "{}")
      stubPutHttpClientCall(url, Json.toJson(updateEmploymentModel).toString(), httpResponse, headersSentToIntegrationFramework)

      val result = await(connector.updateEmployment(nino, taxYear, employmentId, updateEmploymentModel)(hc))

      result mustBe Right(())
    }

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"DES returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)
          val httpResponse = HttpResponse(status, desError.toJson.toString())

          stubPutHttpClientCall(url, Json.toJson(updateEmploymentModel).toString(), httpResponse)
          val result = await(connector.updateEmployment(nino, taxYear, employmentId, updateEmploymentModel)(hc))
          result mustBe Left(desError)
        }
      }

      s"DES returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val httpResponse = HttpResponse(BAD_GATEWAY, desError.toJson.toString())
        stubPutHttpClientCall(url, Json.toJson(updateEmploymentModel).toString(), httpResponse)

        val result = await(connector.updateEmployment(nino, taxYear, employmentId, updateEmploymentModel))
        result mustBe Left(desError)
      }
    }
  }
}
