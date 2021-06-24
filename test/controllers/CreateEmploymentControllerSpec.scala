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

import connectors.httpParsers.CreateEmploymentHttpParser.CreateEmploymentResponse
import models.shared.{EmploymentRequestModel, AddEmploymentResponseModel}
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalamock.handlers.CallHandler4
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout}
import services.EmploymentService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class CreateEmploymentControllerSpec extends TestUtils {

  val employmentService: EmploymentService = mock[EmploymentService]
  val createEmploymentController = new CreateEmploymentController(employmentService, authorisedAction, mockControllerComponents)

  val nino = "tax_entity_id"
  val taxYear = 2020

  "createEmployment" when {

    def mockCreateOrAmendEmploymentSuccess(): CallHandler4[String, Int, EmploymentRequestModel, HeaderCarrier, Future[CreateEmploymentResponse]] = {
      val response: CreateEmploymentResponse = Right(AddEmploymentResponseModel("employment_id"))
      (employmentService.createEmployment(_: String, _: Int, _: EmploymentRequestModel)(_: HeaderCarrier))
        .expects(*, *, *, *)
        .returning(Future.successful(response))
    }

    def mockCreateOrAmendEmploymentFailure(httpStatus: Int): CallHandler4[String, Int, EmploymentRequestModel, HeaderCarrier, Future[CreateEmploymentResponse]] = {
      val error: CreateEmploymentResponse = Left(DesErrorModel(httpStatus, DesErrorBodyModel("DES_CODE", "DES_REASON")))
      (employmentService.createEmployment(_: String, _: Int, _: EmploymentRequestModel)(_: HeaderCarrier))
        .expects(*, *, *, *)
        .returning(Future.successful(error))
    }

    val mtditid: String = "1234567890"
    val fakeRequest = FakeRequest("POST", "/TBC").withHeaders("mtditid" -> mtditid)

    val requestBody =
      Json.parse(
        """
          |{
          |  "employerRef": "employerRef",
          |  "employerName": "employerName",
          |  "startDate": "2021-06-11T16:44:37.410+01:00",
          |  "cessationDate": "2021-06-11T16:44:37.424+01:00",
          |  "payrollId": "payrollId"
          |}
          |""".stripMargin)

    val requestBodyWithOnlyMandatoryValues =
      Json.parse(
        """
          |{
          |  "employerName": "employerName",
          |  "startDate": "2021-06-11T16:44:37.410+01:00"
          |}
          |""".stripMargin)

    val invalidRequestBody =
      Json.parse(
        """
          |{
          |  "unknown_key": "value"
          |}
          |""".stripMargin)

    "request is from Individual" should {
      "return a 200 response with employmentId" in {
        val result = {
          mockAuth()
          mockCreateOrAmendEmploymentSuccess()
          createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(requestBody))
        }
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("employmentId" -> "employment_id")
      }

      "return a 200 response with employmentId when request body only has mandatory values" in {
        val result = {
          mockAuth()
          mockCreateOrAmendEmploymentSuccess()
          createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(requestBodyWithOnlyMandatoryValues))
        }
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("employmentId" -> "employment_id")
      }

      "return a 400 response when request body is not valid" in {
        val result = {
          mockAuth()
          createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(invalidRequestBody))
        }
        status(result) mustBe BAD_REQUEST

      }

      Seq(UNAUTHORIZED, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuth()
            mockCreateOrAmendEmploymentFailure(httpErrorCode)
            createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(requestBody))
          }
          status(result) mustBe httpErrorCode
          contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE" , "reason" -> "DES_REASON")
        }
      }

    }

    "request is from Agent" should {
      "return a 200 response with employmentId" in {
        val result = {
          mockAuthAsAgent()
          mockCreateOrAmendEmploymentSuccess()
          createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(requestBody))
        }
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("employmentId" -> "employment_id")
      }

      "return a 200 response with employmentId when request body only has mandatory values" in {
        val result = {
          mockAuthAsAgent()
          mockCreateOrAmendEmploymentSuccess()
          createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(requestBodyWithOnlyMandatoryValues))
        }
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj("employmentId" -> "employment_id")
      }

      "return a 400 response when request body is not valid" in {
        val result = {
          mockAuthAsAgent()
          createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(invalidRequestBody))
        }
        status(result) mustBe BAD_REQUEST

      }

      Seq(UNAUTHORIZED, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuthAsAgent()
            mockCreateOrAmendEmploymentFailure(httpErrorCode)
            createEmploymentController.createEmployment(nino, taxYear)(fakeRequest.withJsonBody(requestBody))
          }
          status(result) mustBe httpErrorCode
          contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE" , "reason" -> "DES_REASON")
        }
      }
    }

  }
}
