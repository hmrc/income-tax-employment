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

import connectors.httpParsers.UpdateEmploymentDataHttpParser.UpdateEmploymentDataResponse
import models.shared.EmploymentRequestModel
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalamock.handlers.CallHandler5
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout}
import services.EmploymentService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class UpdateEmploymentControllerSpec extends TestUtils {

  val employmentService: EmploymentService = mock[EmploymentService]
  val updateEmploymentController = new UpdateEmploymentController(employmentService, authorisedAction, mockControllerComponents)

  val nino = "tax_entity_id"
  val employmentId = "employment_id"
  val taxYear = 2020
  val requestBody: JsValue =
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

  val requestBodyWithOnlyMandatoryValues: JsValue =
    Json.parse(
      """
        |{
        |  "employerName": "employerName",
        |  "startDate": "2021-06-11T16:44:37.410+01:00"
        |}
        |""".stripMargin)

  val invalidRequestBody: JsValue =
    Json.parse(
      """
        |{
        |  "unknown_key": "value"
        |}
        |""".stripMargin)

  "UpdateEmployment" when {

    def mockUpdateEmploymentSuccess(): CallHandler5[String, Int, String, EmploymentRequestModel, HeaderCarrier, Future[UpdateEmploymentDataResponse]] = {
      val response: UpdateEmploymentDataResponse = Right(())
      (employmentService.updateEmployment(_: String, _: Int, _: String, _: EmploymentRequestModel)(_: HeaderCarrier))
        .expects(*, *, *, *, *)
        .returning(Future.successful(response))
    }

    def mockUpdateEmploymentFailure(httpStatus: Int): CallHandler5[String, Int, String, EmploymentRequestModel, HeaderCarrier,
      Future[UpdateEmploymentDataResponse]] = {
      val error: UpdateEmploymentDataResponse = Left(DesErrorModel(httpStatus, DesErrorBodyModel("DES_CODE", "DES_REASON")))
      (employmentService.updateEmployment(_: String, _: Int, _: String, _: EmploymentRequestModel)(_: HeaderCarrier))
        .expects(*, *, *, *, *)
        .returning(Future.successful(error))
    }

    val mtditid: String = "1234567890"
    val fakeRequest = FakeRequest("PUT", "/TBC").withHeaders("mtditid" -> mtditid)

    "request is from Individual" should {
      "return a 204 response when Update is successful" in {
        val result = {
          mockAuth()
          mockUpdateEmploymentSuccess()
          updateEmploymentController.updateEmployment(nino, taxYear, employmentId)(fakeRequest.withJsonBody(requestBodyWithOnlyMandatoryValues))
        }
        status(result) mustBe NO_CONTENT
      }

      "return a 400 response when request body is not valid" in {
        val result = {
          mockAuth()
          updateEmploymentController.updateEmployment(nino, taxYear, employmentId)(fakeRequest.withJsonBody(invalidRequestBody))
        }
        status(result) mustBe BAD_REQUEST
      }

      Seq(UNAUTHORIZED, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuth()
            mockUpdateEmploymentFailure(httpErrorCode)
            updateEmploymentController.updateEmployment(nino, taxYear, employmentId)(fakeRequest.withJsonBody(requestBody))
          }

          status(result) mustBe httpErrorCode
          contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE" , "reason" -> "DES_REASON")
        }
      }

    }

    "request is from Agent" should {
      "return a 204 response when Update is successful" in {
        val result = {
          mockAuthAsAgent()
          mockUpdateEmploymentSuccess()
          updateEmploymentController.updateEmployment(nino, taxYear, employmentId)(fakeRequest.withJsonBody(requestBody))
        }
        status(result) mustBe NO_CONTENT
      }

      "return a 400 response when request body is not valid" in {
        val result = {
          mockAuthAsAgent()
          updateEmploymentController.updateEmployment(nino, taxYear, employmentId)(fakeRequest.withJsonBody(invalidRequestBody))
        }
        status(result) mustBe BAD_REQUEST
      }

      Seq(UNAUTHORIZED, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuthAsAgent()
            mockUpdateEmploymentFailure(httpErrorCode)
            updateEmploymentController.updateEmployment(nino, taxYear, employmentId)(fakeRequest.withJsonBody(requestBody))
          }
          status(result) mustBe httpErrorCode
          contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE" , "reason" -> "DES_REASON")
        }
      }
    }

  }
}
