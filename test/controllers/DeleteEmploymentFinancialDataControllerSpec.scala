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

import models.{DesErrorBodyModel, DesErrorModel}
import org.scalamock.handlers.CallHandler4
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout}
import services.EmploymentService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class DeleteEmploymentFinancialDataControllerSpec extends TestUtils {

  val employmentService: EmploymentService = mock[EmploymentService]
  val deleteEmploymentFinancialDataController = new DeleteEmploymentFinancialDataController(employmentService, authorisedAction, mockControllerComponents)

  val nino = "tax_entity_id"
  val employmentId = "employment_id"
  val taxYear = 2020

  "deleteEmploymentFinancialData" when {

    def mockDeleteEmploymentFinancialDataSuccess(): CallHandler4[String, Int, String, HeaderCarrier, Future[Either[DesErrorModel, Unit]]] = {
      val response: Either[DesErrorModel, Unit] = Right(())
      (employmentService.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(*, *, *, *)
        .returning(Future.successful(response))
    }

    def mockDeleteEmploymentFinancialDataFailure(httpStatus: Int): CallHandler4[String, Int, String,
      HeaderCarrier, Future[Either[DesErrorModel, Unit]]] = {
      val error: Either[DesErrorModel, Unit] = Left(DesErrorModel(httpStatus, DesErrorBodyModel("DES_CODE", "DES_REASON")))
      (employmentService.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(*, *, *, *)
        .returning(Future.successful(error))
    }

    val mtditid: String = "1234567890"
    val fakeRequest = FakeRequest("DELETE", "/TBC").withHeaders("mtditid" -> mtditid)

    "request is from Individual" should {
      "return a 204 response when delete is successful" in {
        val result = {
          mockAuth()
          mockDeleteEmploymentFinancialDataSuccess()
          deleteEmploymentFinancialDataController.deleteEmploymentFinancialData(nino, taxYear, employmentId)(fakeRequest)
        }
        status(result) mustBe NO_CONTENT
      }

      Seq(NOT_FOUND, BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuth()
            mockDeleteEmploymentFinancialDataFailure(httpErrorCode)
            deleteEmploymentFinancialDataController.deleteEmploymentFinancialData(nino, taxYear, employmentId)(fakeRequest)
          }

          status(result) mustBe httpErrorCode
          contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE" , "reason" -> "DES_REASON")
        }
      }

    }

    "request is from Agent" should {
      "return a 204 response when delete is successful" in {
        val result = {
          mockAuthAsAgent()
          mockDeleteEmploymentFinancialDataSuccess()
          deleteEmploymentFinancialDataController.deleteEmploymentFinancialData(nino, taxYear, employmentId)(fakeRequest)
        }
        status(result) mustBe NO_CONTENT
      }

      Seq(NOT_FOUND, BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuthAsAgent()
            mockDeleteEmploymentFinancialDataFailure(httpErrorCode)
            deleteEmploymentFinancialDataController.deleteEmploymentFinancialData(nino, taxYear, employmentId)(fakeRequest)
          }
          status(result) mustBe httpErrorCode
          contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE" , "reason" -> "DES_REASON")
        }
      }
    }

  }
}

