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

package controllers

import connectors.errors.{ApiError, SingleErrorBody}
import connectors.parsers.GetEmploymentDataHttpParser.GetEmploymentDataResponse
import org.scalamock.handlers.CallHandler5
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import services.EmploymentOrchestrationService
import support.helpers.MockAuthHelper
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestData.customerEmploymentDataModelExample

import scala.concurrent.Future

class GetEmploymentDataControllerSpec extends AnyWordSpec with MockAuthHelper {

  val getEmploymentDataService: EmploymentOrchestrationService = mock[EmploymentOrchestrationService]
  val getEmploymentDataController = new GetEmploymentDataController(getEmploymentDataService, authorisedAction, mockControllerComponents)
  val nino: String = "123456789"
  val mtdItID: String = "123123123"
  val taxYear: Int = 1234
  val view = "CUSTOMER"
  val employmentId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934"
  val badRequestModel: SingleErrorBody = SingleErrorBody("INVALID_NINO", "Nino is invalid")
  val notFoundModel: SingleErrorBody = SingleErrorBody("NOT_FOUND_INCOME_SOURCE", "Can't find income source")
  val serverErrorModel: SingleErrorBody = SingleErrorBody("SERVER_ERROR", "Internal server error")
  val serviceUnavailableErrorModel: SingleErrorBody = SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable")
  private val fakeGetRequest = FakeRequest("GET", "/").withHeaders("MTDITID" -> "1234567890")

  def mockGetEmploymentDataValid(): CallHandler5[String, Int, String, String, HeaderCarrier, Future[GetEmploymentDataResponse]] = {
    val validEmploymentData: GetEmploymentDataResponse = Right(Some(customerEmploymentDataModelExample))
    (getEmploymentDataService.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(validEmploymentData))
  }

  def mockGetEmploymentDataBadRequest(): CallHandler5[String, Int, String, String, HeaderCarrier, Future[GetEmploymentDataResponse]] = {
    val invalidEmploymentData: GetEmploymentDataResponse = Left(ApiError(BAD_REQUEST, badRequestModel))
    (getEmploymentDataService.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentData))
  }

  def mockGetEmploymentDataNotFound(): CallHandler5[String, Int, String, String, HeaderCarrier, Future[GetEmploymentDataResponse]] = {
    (getEmploymentDataService.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(Right(None)))
  }

  def mockGetEmploymentDataServerError(): CallHandler5[String, Int, String, String, HeaderCarrier, Future[GetEmploymentDataResponse]] = {
    val invalidEmploymentData: GetEmploymentDataResponse = Left(ApiError(INTERNAL_SERVER_ERROR, serverErrorModel))
    (getEmploymentDataService.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentData))
  }

  def mockGetEmploymentDataServiceUnavailable(): CallHandler5[String, Int, String, String, HeaderCarrier, Future[GetEmploymentDataResponse]] = {
    val invalidEmploymentData: GetEmploymentDataResponse = Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
    (getEmploymentDataService.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentData))
  }

  "calling .getEmploymentData" should {

    "with existing employments" should {

      "return an OK 200 response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentDataValid()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe OK
      }

      "return an OK 200 response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentDataValid()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe OK
      }

    }

    "without existing data" should {

      "return an NoContent response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentDataNotFound()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe NO_CONTENT
      }

      "return an NotFound response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentDataNotFound()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe NO_CONTENT
      }

    }

    "with an invalid NINO" should {

      "return a BadRequest response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentDataBadRequest()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe BAD_REQUEST
        Json.parse(contentAsString(result)) mustBe Json.parse("""{"code":"INVALID_NINO","reason":"Nino is invalid"}""".stripMargin)
      }

      "return a BadRequest response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentDataBadRequest()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe BAD_REQUEST
        Json.parse(contentAsString(result)) mustBe Json.parse("""{"code":"INVALID_NINO","reason":"Nino is invalid"}""".stripMargin)
      }
    }

    "with an invalid view" should {

      "return an BadRequest response when called as an individual" in {
        val result = {
          mockAuth()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, "invalid_view")(fakeGetRequest)
        }
        status(result) mustBe BAD_REQUEST
        Json.parse(contentAsString(result)) mustBe
          Json.parse("""{"code":"INVALID_VIEW","reason":"Submission has not passed validation. Invalid query parameter view."}""".stripMargin)
      }
    }

    "with something that causes an internal server error in DES" should {

      "return an INTERNAL_SERVER_ERROR response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentDataServerError()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
        Json.parse(contentAsString(result)) mustBe Json.parse("""{"code":"SERVER_ERROR","reason":"Internal server error"}""".stripMargin)
      }

      "return an INTERNAL_SERVER_ERROR response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentDataServerError()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
        Json.parse(contentAsString(result)) mustBe Json.parse("""{"code":"SERVER_ERROR","reason":"Internal server error"}""".stripMargin)
      }
    }

    "with an unavailable service" should {

      "return a Service_Unavailable response when called as an individual" in {
        val result = {
          mockAuth()
          mockGetEmploymentDataServiceUnavailable()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
        Json.parse(contentAsString(result)) mustBe Json.parse("""{"code":"SERVICE_UNAVAILABLE","reason":"Service is unavailable"}""".stripMargin)
      }

      "return a Service_Unavailable response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockGetEmploymentDataServiceUnavailable()
          getEmploymentDataController.getEmploymentData(nino, taxYear, employmentId, view)(fakeGetRequest)
        }
        status(result) mustBe SERVICE_UNAVAILABLE
        Json.parse(contentAsString(result)) mustBe Json.parse("""{"code":"SERVICE_UNAVAILABLE","reason":"Service is unavailable"}""".stripMargin)
      }
    }

  }
}
