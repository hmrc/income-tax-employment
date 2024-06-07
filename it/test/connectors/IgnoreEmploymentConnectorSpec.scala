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
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import support.helpers.WiremockSpec
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter
import utils.TaxYearUtils.toTaxYearParam

class IgnoreEmploymentConnectorSpec extends PlaySpec with WiremockSpec {

  lazy val connector: IgnoreEmploymentConnector = app.injector.instanceOf[IgnoreEmploymentConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(integrationFrameworkHost: String): BackendAppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override lazy val integrationFrameworkBaseUrl: String = s"http://$integrationFrameworkHost:$wireMockPort"
  }

  val appConfigWithInternalHost: BackendAppConfig = appConfig("localhost")
  val appConfigWithExternalHost: BackendAppConfig = appConfig("127.0.0.1")
  val nino = "taxable_entity_id"
  val employmentId = "employment_id"

  ".IgnoreEmploymentConnector before 23-24" should {

    val taxYear = 2022
    val url = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId/ignore"

    "include internal headers" when {

      val headersSentToIntegrationFramework = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new IgnoreEmploymentConnector(httpClient, appConfigWithInternalHost)

        stubPutWithoutResponseBody(url, "{}", NO_CONTENT, headersSentToIntegrationFramework)

        val result = await(connector.ignoreEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Right(())
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new IgnoreEmploymentConnector(httpClient, appConfigWithExternalHost)

        stubPutWithoutResponseBody(url, "{}", NO_CONTENT, headersSentToIntegrationFramework)

        val result = await(connector.ignoreEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Right(())
      }
    }

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"Integration Framework returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubPutWithResponseAndWithoutRequestBody(url, status, desError.toJson.toString())
          val result = await(connector.ignoreEmployment(nino, taxYear, employmentId))
          result mustBe Left(desError)
        }
      }

      s"Integration Framework returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubPutWithResponseAndWithoutRequestBody(url, BAD_GATEWAY, desError.toJson.toString())

        val result = await(connector.ignoreEmployment(nino, taxYear, employmentId))

        result mustBe Left(desError)
      }
    }

  }

  ".IgnoreEmploymentConnector after 23-24" should {

    val taxYear = 2024
    val url = s"/income-tax/${toTaxYearParam(taxYear)}/income/employments/$nino/$employmentId/ignore"


    "include internal headers" when {

      val headersSentToIntegrationFramework = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new IgnoreEmploymentConnector(httpClient, appConfigWithInternalHost)

        stubPutWithoutResponseBody(url, "{}", NO_CONTENT, headersSentToIntegrationFramework)

        val result = await(connector.ignoreEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Right(())
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new IgnoreEmploymentConnector(httpClient, appConfigWithExternalHost)

        stubPutWithoutResponseBody(url, "{}", NO_CONTENT, headersSentToIntegrationFramework)

        val result = await(connector.ignoreEmployment(nino, taxYear, employmentId)(hc))

        result mustBe Right(())
      }
    }

    "handle error" when {
      val desErrorBodyModel = SingleErrorBody("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"Integration Framework returns $status" in {
          val desError = ApiError(status, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubPutWithResponseAndWithoutRequestBody(url, status, desError.toJson.toString())
          val result = await(connector.ignoreEmployment(nino, taxYear, employmentId))
          result mustBe Left(desError)
        }
      }

      s"Integration Framework returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubPutWithResponseAndWithoutRequestBody(url, BAD_GATEWAY, desError.toJson.toString())

        val result = await(connector.ignoreEmployment(nino, taxYear, employmentId))

        result mustBe Left(desError)
      }
    }

  }

}
