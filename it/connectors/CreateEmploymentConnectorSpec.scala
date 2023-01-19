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
import config.BackendAppConfig
import connectors.errors.{SingleErrorBody, ApiError}
import models.shared.{AddEmploymentResponseModel, CreateUpdateEmployment}
import org.joda.time.DateTime.now
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import support.helpers.WiremockSpec
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter

class CreateEmploymentConnectorSpec extends PlaySpec with WiremockSpec {

  lazy val connector: CreateEmploymentConnector = app.injector.instanceOf[CreateEmploymentConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  def appConfig(integrationFrameworkHost: String): BackendAppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override lazy val integrationFrameworkBaseUrl: String = s"http://$integrationFrameworkHost:$wireMockPort"
  }

  val taxYear = 2022

  ".CreateEmploymentConnector" should {

    val appConfigWithInternalHost = appConfig("localhost")
    val appConfigWithExternalHost = appConfig("127.0.0.1")

    val nino = "taxable_entity_id"

    val addEmploymentModel = CreateUpdateEmployment(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))

    val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/custom"

    "include internal headers" when {
      val expectedResult = AddEmploymentResponseModel("employmendId")
      val responseBody = Json.toJson(expectedResult).toString()
      val requestBody = Json.toJson(addEmploymentModel).toString()

      val headersSentToIntegrationFramework = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      "the host for Integration Framework is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateEmploymentConnector(httpClient, appConfigWithInternalHost)

        stubPostWithResponseBody(url, OK, requestBody, responseBody, headersSentToIntegrationFramework)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for Integration Framework is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateEmploymentConnector(httpClient, appConfigWithExternalHost)

        stubPostWithResponseBody(url, OK, requestBody, responseBody, headersSentToIntegrationFramework)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return error" when {
      val expectedResult = Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError(true)))

      "when Integration Framework returns 200 but the schema of the json response body is unexpected" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateEmploymentConnector(httpClient, appConfigWithInternalHost)

        stubPostWithResponseBody(url, OK, Json.toJson(addEmploymentModel).toString(), "{}")

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe expectedResult
      }
    }

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      val requestBody = Json.toJson(addEmploymentModel).toString()

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"DES returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubPostWithResponseBody(url, status, requestBody, desError.toJson.toString())

          val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel))

          result mustBe Left(desError)
        }
      }

      s"Integration Framework returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubPostWithResponseBody(url, BAD_GATEWAY, requestBody, desError.toJson.toString())

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel))

        result mustBe Left(desError)
      }

    }
  }
}
