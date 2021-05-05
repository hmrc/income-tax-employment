///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers
//
//import connectors.httpParsers.GetEmploymentListHttpParser.GetEmploymentListResponse
//import models.{DesErrorBodyModel, DesErrorModel}
//import org.scalamock.handlers.CallHandler4
//import play.api.http.Status._
//import play.api.test.FakeRequest
//import services.GetEmploymentListService
//import uk.gov.hmrc.http.HeaderCarrier
//import utils.TestUtils
//
//import scala.concurrent.Future
//
//class GetEmploymentListControllerSpec extends TestUtils {
//
//  val getEmploymentListService: GetEmploymentListService = mock[GetEmploymentListService]
//  val getEmploymentListController = new GetEmploymentListController(getEmploymentListService,authorisedAction, mockControllerComponents)
//  val nino :String = "123456789"
//  val mtdItID :String = "123123123"
//  val taxYear: Int = 1234
//  val badRequestModel: DesErrorBodyModel = DesErrorBodyModel("INVALID_NINO", "Nino is invalid")
//  val notFoundModel: DesErrorBodyModel = DesErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find income source")
//  val serverErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVER_ERROR", "Internal server error")
//  val serviceUnavailableErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable")
//  private val fakeGetRequest = FakeRequest("GET", "/").withHeaders("MTDITID" -> "1234567890")
//
//  def mockGetEmploymentListValid(): CallHandler4[String, Int, Option[String], HeaderCarrier, Future[GetEmploymentListResponse]] = {
//    val validEmploymentList: GetEmploymentListResponse = Right(getEmploymentListModelExample)
//    (getEmploymentListService.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
//      .expects(*, *, *, *)
//      .returning(Future.successful(validEmploymentList))
//  }
//
//  def mockGetEmploymentListValidWithNoData(): CallHandler4[String, Int, Option[String], HeaderCarrier, Future[GetEmploymentListResponse]] = {
//    val validEmploymentList: GetEmploymentListResponse = Right(getEmploymentListModelExampleWithNoData)
//    (getEmploymentListService.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
//      .expects(*, *, *, *)
//      .returning(Future.successful(validEmploymentList))
//  }
//
//  def mockGetEmploymentListBadRequest(): CallHandler4[String, Int, Option[String], HeaderCarrier, Future[GetEmploymentListResponse]] = {
//    val invalidEmploymentList: GetEmploymentListResponse = Left(DesErrorModel(BAD_REQUEST, badRequestModel))
//    (getEmploymentListService.getEmploymentList(_: String, _: Int, _: Option[String])(_: HeaderCarrier))
//      .expects(*, *, *, *)
//      .returning(Future.successful(invalidEmploymentList))
//  }
//
//  def mockGetEmploymentListNotFound(): CallHandler4[String, Int, Option[String], HeaderCarrier, Future[GetEmploymentListResponse]] = {
//    val invalidEmploymentList: GetEmploymentListResponse = Left(DesErrorModel(NOT_FOUND, notFoundModel))
//    (getEmploymentListService.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
//      .expects(*, *, *, *)
//      .returning(Future.successful(invalidEmploymentList))
//  }
//
//  def mockGetEmploymentListServerError(): CallHandler4[String, Int, Option[String], HeaderCarrier, Future[GetEmploymentListResponse]] = {
//    val invalidEmploymentList: GetEmploymentListResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, serverErrorModel))
//    (getEmploymentListService.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
//      .expects(*, *, *, *)
//      .returning(Future.successful(invalidEmploymentList))
//  }
//
//  def mockGetEmploymentListServiceUnavailable(): CallHandler4[String, Int, Option[String], HeaderCarrier, Future[GetEmploymentListResponse]] = {
//    val invalidEmploymentList: GetEmploymentListResponse = Left(DesErrorModel(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
//    (getEmploymentListService.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
//      .expects(*, *, *, *)
//      .returning(Future.successful(invalidEmploymentList))
//  }
//
//  "calling .getEmploymentList" should {
//
//    "with existing employments" should {
//
//      "return an OK 200 response when called as an individual" in {
//        val result = {
//          mockAuth()
//          mockGetEmploymentListValid()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe OK
//      }
//
//      "return an OK 200 response when called as an agent" in {
//        val result = {
//          mockAuthAsAgent()
//          mockGetEmploymentListValid()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe OK
//      }
//
//      "return a NO_CONTENT response when no data is returned" in {
//        val result = {
//          mockAuthAsAgent()
//          mockGetEmploymentListValidWithNoData()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe NO_CONTENT
//      }
//
//    }
//
//    "without existing employments" should {
//
//      "return an NotFound response when called as an individual" in {
//        val result = {
//          mockAuth()
//          mockGetEmploymentListNotFound()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe NOT_FOUND
//      }
//
//      "return an NotFound response when called as an agent" in {
//        val result = {
//          mockAuthAsAgent()
//          mockGetEmploymentListNotFound()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe NOT_FOUND
//      }
//
//    }
//
//    "with an invalid NINO" should {
//
//      "return an BadRequest response when called as an individual" in {
//        val result = {
//          mockAuth()
//          mockGetEmploymentListBadRequest()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe BAD_REQUEST
//      }
//
//      "return an BadRequest response when called as an agent" in {
//        val result = {
//          mockAuthAsAgent()
//          mockGetEmploymentListBadRequest()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe BAD_REQUEST
//      }
//    }
//
//    "with something that causes and internal server error in DES" should {
//
//      "return an BadRequest response when called as an individual" in {
//        val result = {
//          mockAuth()
//          mockGetEmploymentListServerError()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe INTERNAL_SERVER_ERROR
//      }
//
//      "return an BadRequest response when called as an agent" in {
//        val result = {
//          mockAuthAsAgent()
//          mockGetEmploymentListServerError()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe INTERNAL_SERVER_ERROR
//      }
//    }
//
//    "with an unavailable service" should {
//
//      "return an Service_Unavailable response when called as an individual" in {
//        val result = {
//          mockAuth()
//          mockGetEmploymentListServiceUnavailable()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe SERVICE_UNAVAILABLE
//      }
//
//      "return an Service_Unavailable response when called as an agent" in {
//        val result = {
//          mockAuthAsAgent()
//          mockGetEmploymentListServiceUnavailable()
//          getEmploymentListController.getEmploymentList(nino, taxYear, None)(fakeGetRequest)
//        }
//        status(result) mustBe SERVICE_UNAVAILABLE
//      }
//    }
//
//  }
//}
