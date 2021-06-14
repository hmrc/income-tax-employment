/*
 * Copyright 2021 HM Revenue & Customs
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
import config.AppConfig
import helpers.WiremockSpec
import models.shared.{AddEmploymentRequestModel, AddEmploymentResponseModel}
import models.{DesErrorBodyModel, DesErrorModel}
import org.joda.time.DateTime.now
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter

class CreateEmploymentConnectorSpec extends PlaySpec with WiremockSpec {

  lazy val connector: CreateEmploymentConnector = app.injector.instanceOf[CreateEmploymentConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  def appConfig(desHost: String): AppConfig = new AppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val desBaseUrl: String = s"http://$desHost:$wireMockPort"
  }

  val taxYear = 2022

  ".CreateEmploymentConnector" should {

    val appConfigWithInternalHost = appConfig("localhost")
    val appConfigWithExternalHost = appConfig("127.0.0.1")

    val nino = "taxable_entity_id"

    val addEmploymentModel = AddEmploymentRequestModel(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))

    val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/custom"

    "include internal headers" when {
      val expectedResult = AddEmploymentResponseModel("employmendId")
      val desResponseBody = Json.toJson(expectedResult).toString()
      val desRequestBody = Json.toJson(addEmploymentModel).toString()

      val headersSentToBenefits = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      "the host for DES is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateEmploymentConnector(httpClient, appConfigWithInternalHost)

        stubPostWithResponseBody(url, OK, desRequestBody, desResponseBody, headersSentToBenefits)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for DES is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateEmploymentConnector(httpClient, appConfigWithExternalHost)

        stubPostWithResponseBody(url, OK, desRequestBody, desResponseBody, headersSentToBenefits)

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return error" when {
      val expectedResult = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError(true)))

      "when des returns 200 but the schema of the json response body is unexpected" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new CreateEmploymentConnector(httpClient, appConfigWithInternalHost)

        stubPostWithResponseBody(url, OK, Json.toJson(addEmploymentModel).toString(), "{}")

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel)(hc))

        result mustBe expectedResult
      }
    }

    "handle error" when {
      val desErrorBodyModel = DesErrorBodyModel("DES_CODE", "DES_REASON")

      val desRequestBody = Json.toJson(addEmploymentModel).toString()

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"DES returns $status" in {
          val desError = DesErrorModel(status, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubPostWithResponseBody(url, status, desRequestBody, desError.toJson.toString())

          val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel))

          result mustBe Left(desError)
        }
      }

      s"DES returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubPostWithResponseBody(url, BAD_GATEWAY, desRequestBody, desError.toJson.toString())

        val result = await(connector.createEmployment(nino, taxYear, addEmploymentModel))

        result mustBe Left(desError)
      }

    }
  }
}
