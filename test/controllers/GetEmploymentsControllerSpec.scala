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

package controllers

import models.frontend.AllEmploymentData
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalamock.handlers.CallHandler5
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.EmploymentOrchestrationService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class GetEmploymentsControllerSpec extends TestUtils {

  val service: EmploymentOrchestrationService = mock[EmploymentOrchestrationService]
  val controller = new GetEmploymentsController(service,authorisedAction, mockControllerComponents)
  val nino :String = "123456789"
  val mtdItID :String = "123123123"
  val taxYear: Int = 1234
  val badRequestModel: DesErrorBodyModel = DesErrorBodyModel("INVALID_NINO", "Nino is invalid")
  val notFoundModel: DesErrorBodyModel = DesErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find income source")
  val serverErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVER_ERROR", "Internal server error")
  val serviceUnavailableErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable")
  private val fakeGetRequest = FakeRequest("GET", "/").withHeaders("MTDITID" -> "1234567890")

  def mockGetEmploymentListValid(): CallHandler5[String, Int, String, HeaderCarrier, ExecutionContext,
    Future[Either[DesErrorModel, AllEmploymentData]]] = {
    (service.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(Right(allEmploymentData)))
  }

  def mockGetEmploymentListValidWithNoData(): CallHandler5[String, Int, String,
    HeaderCarrier, ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val validEmploymentList = Right(AllEmploymentData(Seq(),None,Seq(),None))
    (service.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(validEmploymentList))
  }

  def mockGetEmploymentListBadRequest(): CallHandler5[String, Int, String,HeaderCarrier,
    ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList = Left(DesErrorModel(BAD_REQUEST, badRequestModel))
    (service.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentList))
  }

  def mockGetEmploymentListNotFound(): CallHandler5[String, Int, String,HeaderCarrier,
    ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList = Left(DesErrorModel(NOT_FOUND, notFoundModel))
    (service.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentList))
  }

  def mockGetEmploymentListServerError(): CallHandler5[String, Int, String,HeaderCarrier,
    ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList= Left(DesErrorModel(INTERNAL_SERVER_ERROR, serverErrorModel))
    (service.getAllEmploymentData(_: String, _: Int, _:String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentList))
  }

  def mockGetEmploymentListServiceUnavailable(): CallHandler5[String, Int, String,HeaderCarrier,
    ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList = Left(DesErrorModel(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
    (service.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentList))
  }

  "calling .getEmploymentList" should {

    "with existing employments" should {

      "return an OK 200 response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentListValid()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe OK
        Json.parse(bodyOf(result)) mustBe Json.parse(
        """{
          |  "hmrcEmploymentData": [
          |    {
          |      "employmentId": "00000000-0000-0000-1111-000000000000",
          |      "employerName": "Business",
          |      "employerRef": "666/66666",
          |      "payrollId": "1234567890",
          |      "startDate": "2020-01-01",
          |      "cessationDate": "2020-01-01",
          |      "dateIgnored": "2020-01-01T10:00:38Z",
          |      "employmentData": {
          |        "submittedOn": "2020-01-04T05:01:01Z",
          |        "employmentSequenceNumber": "1002",
          |        "companyDirector": false,
          |        "closeCompany": true,
          |        "directorshipCeasedDate": "2020-02-12",
          |        "occPen": false,
          |        "disguisedRemuneration": false,
          |        "pay": {
          |          "taxablePayToDate": 34234.15,
          |          "totalTaxToDate": 6782.92,
          |          "payFrequency": "CALENDAR MONTHLY",
          |          "paymentDate": "2020-04-23",
          |          "taxWeekNo": 32,
          |          "taxMonthNo": 2
          |        },
          |        "deductions": {
          |          "studentLoans": {
          |            "uglDeductionAmount": 100,
          |            "pglDeductionAmount": 100
          |          }
          |        }
          |      },
          |      "employmentBenefits": {
          |        "submittedOn": "2020-01-04T05:01:01Z",
          |        "benefits": {
          |          "accommodation": 100,
          |          "assets": 100,
          |          "assetTransfer": 100,
          |          "beneficialLoan": 100,
          |          "car": 100,
          |          "carFuel": 100,
          |          "educationalServices": 100,
          |          "entertaining": 100,
          |          "expenses": 100,
          |          "medicalInsurance": 100,
          |          "telephone": 100,
          |          "service": 100,
          |          "taxableExpenses": 100,
          |          "van": 100,
          |          "vanFuel": 100,
          |          "mileage": 100,
          |          "nonQualifyingRelocationExpenses": 100,
          |          "nurseryPlaces": 100,
          |          "otherItems": 100,
          |          "paymentsOnEmployeesBehalf": 100,
          |          "personalIncidentalExpenses": 100,
          |          "qualifyingRelocationExpenses": 100,
          |          "employerProvidedProfessionalSubscriptions": 100,
          |          "employerProvidedServices": 100,
          |          "incomeTaxPaidByDirector": 100,
          |          "travelAndSubsistence": 100,
          |          "vouchersAndCreditCards": 100,
          |          "nonCash": 100
          |        }
          |      }
          |    }
          |  ],
          |  "hmrcExpenses": {
          |    "submittedOn": "2020-01-04T05:01:01Z",
          |    "dateIgnored": "2020-01-04T05:01:01Z",
          |    "totalExpenses": 800,
          |    "expenses": {
          |      "businessTravelCosts": 100,
          |      "jobExpenses": 100,
          |      "flatRateJobExpenses": 100,
          |      "professionalSubscriptions": 100,
          |      "hotelAndMealExpenses": 100,
          |      "otherAndCapitalAllowances": 100,
          |      "vehicleExpenses": 100,
          |      "mileageAllowanceRelief": 100
          |    }
          |  },
          |  "customerEmploymentData": [
          |    {
          |      "employmentId": "00000000-0000-0000-2222-000000000000",
          |      "employerName": "Business",
          |      "employerRef": "666/66666",
          |      "payrollId": "1234567890",
          |      "startDate": "2020-01-01",
          |      "cessationDate": "2020-01-01",
          |      "submittedOn": "2020-01-01T10:00:38Z",
          |      "employmentData": {
          |        "submittedOn": "2020-01-04T05:01:01Z",
          |        "employmentSequenceNumber": "1002",
          |        "companyDirector": false,
          |        "closeCompany": true,
          |        "directorshipCeasedDate": "2020-02-12",
          |        "occPen": false,
          |        "disguisedRemuneration": false,
          |        "pay": {
          |          "taxablePayToDate": 34234.15,
          |          "totalTaxToDate": 6782.92,
          |          "payFrequency": "CALENDAR MONTHLY",
          |          "paymentDate": "2020-04-23",
          |          "taxWeekNo": 32,
          |          "taxMonthNo": 2
          |        },
          |        "deductions": {
          |          "studentLoans": {
          |            "uglDeductionAmount": 100,
          |            "pglDeductionAmount": 100
          |          }
          |        }
          |      },
          |      "employmentBenefits": {
          |        "submittedOn": "2020-01-04T05:01:01Z",
          |        "benefits": {
          |          "accommodation": 100,
          |          "assets": 100,
          |          "assetTransfer": 100,
          |          "beneficialLoan": 100,
          |          "car": 100,
          |          "carFuel": 100,
          |          "educationalServices": 100,
          |          "entertaining": 100,
          |          "expenses": 100,
          |          "medicalInsurance": 100,
          |          "telephone": 100,
          |          "service": 100,
          |          "taxableExpenses": 100,
          |          "van": 100,
          |          "vanFuel": 100,
          |          "mileage": 100,
          |          "nonQualifyingRelocationExpenses": 100,
          |          "nurseryPlaces": 100,
          |          "otherItems": 100,
          |          "paymentsOnEmployeesBehalf": 100,
          |          "personalIncidentalExpenses": 100,
          |          "qualifyingRelocationExpenses": 100,
          |          "employerProvidedProfessionalSubscriptions": 100,
          |          "employerProvidedServices": 100,
          |          "incomeTaxPaidByDirector": 100,
          |          "travelAndSubsistence": 100,
          |          "vouchersAndCreditCards": 100,
          |          "nonCash": 100
          |        }
          |      }
          |    }
          |  ],
          |  "customerExpenses": {
          |    "submittedOn": "2020-01-04T05:01:01Z",
          |    "dateIgnored": "2020-01-04T05:01:01Z",
          |    "totalExpenses": 800,
          |    "expenses": {
          |      "businessTravelCosts": 100,
          |      "jobExpenses": 100,
          |      "flatRateJobExpenses": 100,
          |      "professionalSubscriptions": 100,
          |      "hotelAndMealExpenses": 100,
          |      "otherAndCapitalAllowances": 100,
          |      "vehicleExpenses": 100,
          |      "mileageAllowanceRelief": 100
          |    }
          |  }
          |}""".stripMargin)
      }

      "return an OK 200 response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentListValid()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe OK
      }

      "return a NO_CONTENT response when no data is returned" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentListValidWithNoData()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe NO_CONTENT
      }

    }

    "without existing employments" should {

      "return an NotFound response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentListNotFound()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe NOT_FOUND
      }

      "return an NotFound response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentListNotFound()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe NOT_FOUND
      }

    }

    "with an invalid NINO" should {

      "return an BadRequest response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentListBadRequest()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe BAD_REQUEST
      }

      "return an BadRequest response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentListBadRequest()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe BAD_REQUEST
      }
    }

    "with something that causes and internal server error in DES" should {

      "return an BadRequest response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentListServerError()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "return an BadRequest response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentListServerError()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "with an unavailable service" should {

      "return an Service_Unavailable response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentListServiceUnavailable()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }

      "return an Service_Unavailable response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentListServiceUnavailable()
          controller.getEmployments(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }

  }
}
