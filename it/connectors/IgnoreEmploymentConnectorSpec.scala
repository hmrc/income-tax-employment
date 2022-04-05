/*
 * Copyright 2022 HM Revenue & Customs
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
import helpers.WiremockSpec
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter

class IgnoreEmploymentConnectorSpec extends PlaySpec with WiremockSpec {

  lazy val connector: IgnoreEmploymentConnector = app.injector.instanceOf[IgnoreEmploymentConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  def appConfig(integrationFrameworkHost: String): BackendAppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val integrationFrameworkBaseUrl: String = s"http://$integrationFrameworkHost:$wireMockPort"
  }

  val taxYear = 2022

  ".IgnoreEmploymentConnector" should {

    val appConfigWithInternalHost = appConfig("localhost")
    val appConfigWithExternalHost = appConfig("127.0.0.1")

    val nino = "taxable_entity_id"
    val employmentId = "employment_id"
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
      val desErrorBodyModel = DesErrorBodyModel("DES_CODE", "DES_REASON")

      Seq(BAD_REQUEST, UNPROCESSABLE_ENTITY, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { status =>
        s"Integration Framework returns $status" in {
          val desError = DesErrorModel(status, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubPutWithResponseAndWithoutRequestBody(url, status, desError.toJson.toString())
          val result = await(connector.ignoreEmployment(nino, taxYear, employmentId))
          result mustBe Left(desError)
        }
      }

      s"Integration Framework returns unexpected error code - BAD_GATEWAY (502)" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubPutWithResponseAndWithoutRequestBody(url, BAD_GATEWAY,desError.toJson.toString())

        val result = await(connector.ignoreEmployment(nino, taxYear, employmentId))

        result mustBe Left(desError)
      }
    }



  }


}
