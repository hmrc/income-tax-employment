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

import helpers.WiremockSpec
import models.DES.{DESEmploymentFinancialData, Employment, PayModel}
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.DESTaxYearHelper.desTaxYearConverter

class CreateUpdateEmploymentFinancialDataConnectorSpec extends PlaySpec with WiremockSpec{

  lazy val connector: CreateUpdateEmploymentFinancialDataConnector = app.injector.instanceOf[CreateUpdateEmploymentFinancialDataConnector]

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val employmentId: String = "0000000-0000000-000000"
  val minEmployment: Employment = Employment(pay = PayModel(taxablePayToDate = 100.00, totalTaxToDate = 100.00, tipsAndOtherPayments = None), None, None, None)
  val minEmploymentFinancialData: DESEmploymentFinancialData = DESEmploymentFinancialData(minEmployment)
  val stubUrl = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId"

  "PutEmploymentFinancialDataConnector" should {
    "return a success result" when {
      "DES Returns a 204 with minimum data sent in the body" in {
        stubPutWithoutResponseBody(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), NO_CONTENT)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }

      "DES Returns a 204 with maximum data sent in the body" in {
        stubPutWithoutResponseBody(stubUrl, Json.toJson(minEmploymentFinancialData).toString(), NO_CONTENT)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

        result mustBe Right(())
      }
    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj(
        "financialData" -> ""
      )

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError())

      stubPutWithResponseBody(stubUrl, OK, Json.toJson(minEmploymentFinancialData).toString(), invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Forbidden" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )
      val expectedResult = DesErrorModel(FORBIDDEN, DesErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find income source"))

      stubPutWithResponseBody(stubUrl, FORBIDDEN, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Internal Server Error" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal server error"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("SERVER_ERROR", "Internal server error"))

      stubPutWithResponseBody(stubUrl, INTERNAL_SERVER_ERROR, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubPutWithResponseBody(stubUrl, SERVICE_UNAVAILABLE, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result with no body" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError())

      stubPostWithoutResponseBody(stubUrl, NO_CONTENT, Json.toJson(minEmploymentFinancialData).toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubPutWithResponseBody(stubUrl, CONFLICT, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel.parsingError())

      stubPutWithResponseBody(stubUrl, CONFLICT, Json.toJson(minEmploymentFinancialData).toString(), responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, minEmploymentFinancialData)(hc))

      result mustBe Left(expectedResult)
    }
  }
}
