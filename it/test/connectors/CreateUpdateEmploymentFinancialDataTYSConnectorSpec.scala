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
import connectors.errors.{ApiError, SingleErrorBody}
import models._
import models.api.{Employment, PayModel}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import support.helpers.WiremockSpec
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearUtils.toTaxYearParam

class CreateUpdateEmploymentFinancialDataTYSConnectorSpec extends PlaySpec with WiremockSpec {

  lazy val connector: CreateUpdateEmploymentFinancialDataTYSConnector = app.injector.instanceOf[CreateUpdateEmploymentFinancialDataTYSConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  val nino: String = "123456789"
  val specificTaxYear: Int = 2024
  val employmentId: String = "0000000-0000000-000000"
  val minEmployment: Employment = Employment(pay = PayModel(taxablePayToDate = 100.00, totalTaxToDate = 100.00, tipsAndOtherPayments = None), None, None, Some(true))
  val minEmploymentFinancialData: api.EmploymentFinancialData = api.EmploymentFinancialData(minEmployment)
  val stubUrl = s"/income-tax/${toTaxYearParam(specificTaxYear)}/income/employments/$nino/$employmentId"

  def appConfig(integrationFrameworkHost: String): BackendAppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override lazy val integrationFrameworkBaseUrl: String = s"http://$integrationFrameworkHost:$wireMockPort"
  }

  "CreateUpdateEmploymentFinancialDataTYSConnector" should {

    val appConfigWithInternalHost = appConfig("localhost")
    val appConfigWithExternalHost = appConfig("127.0.0.1")

    val headersToSend = Seq(
      new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
    )
    "return a success result and include the correct headers" when {

      "Integration Framework Returns a 204 with minimum data sent in the body when host is internal" in {

        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateUpdateEmploymentFinancialDataTYSConnector(httpClient, appConfigWithInternalHost)

        stubPutWithoutResponseBody(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), NO_CONTENT, headersToSend)

        val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }

      "Integration Framework Returns a 204 with minimum data sent in the body when host is external" in {

        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateUpdateEmploymentFinancialDataTYSConnector(httpClient, appConfigWithExternalHost)

        stubPutWithoutResponseBody(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), NO_CONTENT, headersToSend)

        val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }

      "Integration Framework Returns a 204 with maximum data sent in the body" in {
        stubPutWithoutResponseBody(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), NO_CONTENT)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }
    }

    "returns a BAD_REQUEST response" in {
      val expectedResult = ApiError(BAD_REQUEST, SingleErrorBody("INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear."))

      val responseBody = Json.obj(
        "code" -> "INVALID_TAX_YEAR",
        "reason" -> "Submission has not passed validation. Invalid parameter taxYear."
      )

      stubPutWithResponseBody(stubUrl, BAD_REQUEST, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a NOT_FOUND response" in {
      val expectedResult = ApiError(NOT_FOUND, SingleErrorBody("NOT_FOUND", "The remote endpoint has indicated that Income Source not found."))

      val responseBody = Json.obj(
        "code" -> "NOT_FOUND",
        "reason" -> "The remote endpoint has indicated that Income Source not found."
      )

      stubPutWithResponseBody(stubUrl, NOT_FOUND, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a UNPROCESSABLE_ENTITY response" in {
      val expectedResult = ApiError(UNPROCESSABLE_ENTITY, SingleErrorBody("UNPROCESSABLE_ENTITY",
        "The remote endpoint has indicated that this tax year is not supported."))

      val responseBody = Json.obj(
        "code" -> "UNPROCESSABLE_ENTITY",
        "reason" -> "The remote endpoint has indicated that this tax year is not supported."
      )

      stubPutWithResponseBody(stubUrl, UNPROCESSABLE_ENTITY, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj(
        "financialData" -> ""
      )

      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError(false))

      stubPutWithResponseBody(stubUrl, OK, Json.toJson(minEmploymentFinancialData).toString(), invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Internal Server Error" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal server error"
      )
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVER_ERROR", "Internal server error"))

      stubPutWithResponseBody(stubUrl, INTERNAL_SERVER_ERROR, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubPutWithResponseBody(stubUrl, SERVICE_UNAVAILABLE, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result with no body" in {
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError(false))

      stubPutWithoutResponseBody(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), INTERNAL_SERVER_ERROR)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubPutWithResponseBody(stubUrl, CONFLICT, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when Integration Framework throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError(false))

      stubPutWithResponseBody(stubUrl, CONFLICT, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, specificTaxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }
  }
}
