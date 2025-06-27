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
import utils.DESTaxYearHelper._

import scala.concurrent.ExecutionContext.Implicits.global

class DeleteEmploymentConnectorSpec extends ConnectorIntegrationTest {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
  private val connector = new DeleteEmploymentConnector(httpClientV2, appConfigStub)


  val taxYear = 2022

  ".DeleteEmploymentConnector" should {

    val nino = "taxable_entity_id"
    val employmentId = "employment_id"
    val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/custom/$employmentId"

      val headersSentToIntegrationFramework = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      "return a successful response when the call is successful" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val httpResponse = HttpResponse(NO_CONTENT, "")
        stubDeleteHttpClientCall(url, httpResponse, headersSentToIntegrationFramework)
        val result = await(connector.deleteEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Right(())
      }

    "handle error" when {
      val IFErrorBodyModel = SingleErrorBody("IF_CODE", "IF_REASON")

      Seq(BAD_REQUEST, NOT_FOUND, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"IF returns $status" in {
          val IFError = ApiError(status, IFErrorBodyModel)

          val httpResponse = HttpResponse(status, IFError.toJson.toString())
          stubDeleteHttpClientCall(url, httpResponse, headersSentToIntegrationFramework)

          val result = await(connector.deleteEmployment(nino, taxYear, employmentId))

          result mustBe Left(IFError)
        }
      }

      s"IF returns unexpected error code - BAD_GATEWAY (502)" in {
        val IFError = ApiError(INTERNAL_SERVER_ERROR, IFErrorBodyModel)
        val httpResponse = HttpResponse(BAD_GATEWAY, IFError.toJson.toString())
        stubDeleteHttpClientCall(url, httpResponse)

        val result = await(connector.deleteEmployment(nino, taxYear, employmentId))

        result mustBe Left(IFError)
      }

    }
  }
}
