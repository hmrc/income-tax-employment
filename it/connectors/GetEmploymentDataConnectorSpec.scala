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

import connectors.GetEmploymentDataConnectorSpec.{expectedResponseBody, filteredExpectedResponseBody}
import helpers.WiremockSpec
import models.{DesErrorBodyModel, DesErrorModel, GetEmploymentDataModel}
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.DESTaxYearHelper.desTaxYearConverter

class GetEmploymentDataConnectorSpec extends PlaySpec with WiremockSpec{

  lazy val connector: GetEmploymentDataConnector = app.injector.instanceOf[GetEmploymentDataConnector]

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val employmentId: String = "00000000-0000-1000-8000-000000000000"
  val view: String = "CUSTOMER"
  val getEmploymentDataUrl = s"/income-tax/income/employments/$nino/${desTaxYearConverter(taxYear)}/$employmentId\\?view=$view"

  ".GetEmploymentDataConnector" should {
    "return a GetEmploymentDataModel" when {
      "all values are present in the url" in {
        val expectedResult = Json.parse(expectedResponseBody).as[GetEmploymentDataModel]
        stubGetWithResponseBody(getEmploymentDataUrl, OK, expectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc)).right.get

        result.customerAdded mustBe expectedResult.customerAdded
        result.dateIgnored mustBe expectedResult.dateIgnored
        result.employment mustBe expectedResult.employment
        result.source mustBe expectedResult.source
        result.submittedOn mustBe expectedResult.submittedOn
      }
      "only the required values are returned in the response body" in {
        val expectedResult = Json.parse(filteredExpectedResponseBody).as[GetEmploymentDataModel]
        stubGetWithResponseBody(getEmploymentDataUrl, OK, filteredExpectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc)).right.get

        result.employment mustBe expectedResult.employment
        result.submittedOn mustBe expectedResult.submittedOn
      }
    }


    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj(
        "employments" -> ""
      )

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(getEmploymentDataUrl, OK, invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return a NO_CONTENT" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(getEmploymentDataUrl, NO_CONTENT, "{}")
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Bad Request" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

      stubGetWithResponseBody(getEmploymentDataUrl, BAD_REQUEST, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Not found" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )
      val expectedResult = DesErrorModel(NOT_FOUND, DesErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find income source"))

      stubGetWithResponseBody(getEmploymentDataUrl, NOT_FOUND, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal server error" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal server error"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("SERVER_ERROR", "Internal server error"))

      stubGetWithResponseBody(getEmploymentDataUrl, INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(getEmploymentDataUrl, SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithoutResponseBody(getEmploymentDataUrl, NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(getEmploymentDataUrl, CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(getEmploymentDataUrl, CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentData(nino, taxYear, employmentId, view)(hc))

      result mustBe Left(expectedResult)
    }
  }
}

object GetEmploymentDataConnectorSpec {
  val expectedResponseBody: String =
    """
      {
      |  "submittedOn": "2020-01-04T05:01:01Z",
      |  "source": "CUSTOMER",
      |  "customerAdded": "2020-04-04T01:01:01Z",
      |  "dateIgnored": "2020-04-04T01:01:01Z",
      |  "employment": {
      |    "employmentSequenceNumber": "1003",
      |    "payrollId": "123456789999",
      |    "companyDirector": false,
      |    "closeCompany": true,
      |    "directorshipCeasedDate": "2020-02-12",
      |    "startDate": "2019-04-21",
      |    "cessationDate": "2020-03-11",
      |    "occPen": false,
      |    "disguisedRemuneration": false,
      |    "employer": {
      |      "employerRef": "223/AB12399",
      |      "employerName": "maggie"
      |    },
      |    "pay": {
      |      "taxablePayToDate": 34234.15,
      |      "totalTaxToDate": 6782.92,
      |      "tipsAndOtherPayments": 67676,
      |      "payFrequency": "CALENDAR MONTHLY",
      |      "paymentDate": "2020-04-23",
      |      "taxWeekNo": 32,
      |      "taxMonthNo": 2
      |    },
      |    "deductions": {
      |      "studentLoans": {
      |        "uglDeductionAmount": 13343.45,
      |        "pglDeductionAmount": 24242.56
      |      }
      |    },
      |    "benefitsInKind": {
      |      "accommodation": 455.67,
      |      "assets": 435.54,
      |      "assetTransfer": 24.58,
      |      "beneficialLoan": 33.89,
      |      "car": 3434.78,
      |      "carFuel": 34.56,
      |      "educationalServices": 445.67,
      |      "entertaining": 434.45,
      |      "expenses": 3444.32,
      |      "medicalInsurance": 4542.47,
      |      "telephone": 243.43,
      |      "service": 45.67,
      |      "taxableExpenses": 24.56,
      |      "van": 56.29,
      |      "vanFuel": 14.56,
      |      "mileage": 34.23,
      |      "nonQualifyingRelocationExpenses": 54.62,
      |      "nurseryPlaces": 84.29,
      |      "otherItems": 67.67,
      |      "paymentsOnEmployeesBehalf": 67.23,
      |      "personalIncidentalExpenses": 74.29,
      |      "qualifyingRelocationExpenses": 78.24,
      |      "employerProvidedProfessionalSubscriptions": 84.56,
      |      "employerProvidedServices": 56.34,
      |      "incomeTaxPaidByDirector": 67.34,
      |      "travelAndSubsistence": 56.89,
      |      "vouchersAndCreditCards": 34.9,
      |      "nonCash": 23.89
      |    }
      |  }
      |}
      |""".stripMargin

  val filteredExpectedResponseBody: String =
    """
      |{
      |  "submittedOn": "2020-01-04T05:01:01Z",
      |  "employment": {
      |    "employer": {
      |      "employerName": "maggie"
      |    },
      |    "pay": {
      |      "taxablePayToDate": 34234.15,
      |      "totalTaxToDate": 6782.92,
      |      "payFrequency": "CALENDAR MONTHLY",
      |      "paymentDate": "2020-04-23"
      |    }
      |  }
      |}
      |
      |""".stripMargin
}

