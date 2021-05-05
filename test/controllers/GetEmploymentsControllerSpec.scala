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
import org.scalamock.handlers.CallHandler4
import play.api.http.Status._
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

  def mockGetEmploymentListValid(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    (service.getAllEmploymentData(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(Right(allEmploymentData)))
  }

  def mockGetEmploymentListValidWithNoData(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val validEmploymentList = Right(AllEmploymentData(Seq(),Seq()))
    (service.getAllEmploymentData(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(validEmploymentList))
  }

  def mockGetEmploymentListBadRequest(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList = Left(DesErrorModel(BAD_REQUEST, badRequestModel))
    (service.getAllEmploymentData(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(invalidEmploymentList))
  }

  def mockGetEmploymentListNotFound(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList = Left(DesErrorModel(NOT_FOUND, notFoundModel))
    (service.getAllEmploymentData(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(invalidEmploymentList))
  }

  def mockGetEmploymentListServerError(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList= Left(DesErrorModel(INTERNAL_SERVER_ERROR, serverErrorModel))
    (service.getAllEmploymentData(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(invalidEmploymentList))
  }

  def mockGetEmploymentListServiceUnavailable(): CallHandler4[String, Int, HeaderCarrier,
    ExecutionContext, Future[Either[DesErrorModel, AllEmploymentData]]] = {
    val invalidEmploymentList = Left(DesErrorModel(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
    (service.getAllEmploymentData(_: String, _: Int)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
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
