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
import connectors.parsers.OtherEmploymentIncomeHttpParser.OtherEmploymentIncomeResponse
import org.scalamock.handlers.CallHandler5
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout}
import services.OtherEmploymentIncomeService
import support.builders.api.OtherEmploymentIncomeBuilder
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class OtherEmploymentIncomeControllerSpec extends TestUtils {

  val mockOtherEmploymentIncomeService: OtherEmploymentIncomeService = mock[OtherEmploymentIncomeService]
  val otherEmploymentIncomeController = new OtherEmploymentIncomeController(mockOtherEmploymentIncomeService, authorisedAction, mockControllerComponents)
  val nino: String = "AA123456A"
  val mtdItID: String = "123123123"
  val taxYear: Int = 2022
  private val fakeGetRequest = FakeRequest("GET", "/").withHeaders("MTDITID" -> "1234567890")

  def mockGetEmptyOtherIncome(): CallHandler5[String, Int, String, HeaderCarrier, ExecutionContext, Future[OtherEmploymentIncomeResponse]] = {
    val noOtherEmploymentsResponse: OtherEmploymentIncomeResponse = Right(None)
    (mockOtherEmploymentIncomeService.getOtherEmploymentIncome(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(noOtherEmploymentsResponse))
  }

  def mockGetValidOtherIncome(): CallHandler5[String, Int, String, HeaderCarrier, ExecutionContext, Future[OtherEmploymentIncomeResponse]] = {

    val otherEmploymentsResponse = OtherEmploymentIncomeBuilder.anOtherEmploymentIncome
    val validOtherEmploymentsResponse: OtherEmploymentIncomeResponse = Right(Some(otherEmploymentsResponse))
    (mockOtherEmploymentIncomeService.getOtherEmploymentIncome(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(validOtherEmploymentsResponse))
  }

  def mockOtherIncomeFailure(httpErrorCode: Int): CallHandler5[String, Int, String, HeaderCarrier, ExecutionContext, Future[OtherEmploymentIncomeResponse]] = {
    val error = Left(ApiError(httpErrorCode, SingleErrorBody("DES_CODE", "DES_REASON")))
    val errorResponse: OtherEmploymentIncomeResponse = error
    (mockOtherEmploymentIncomeService.getOtherEmploymentIncome(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(errorResponse))
  }

  "For OtherEmploymentsController calling getOtherEmployments" should {

    "return a 204 No Content response with no other employments income" in {

      val result = {
        mockAuth()
        mockGetEmptyOtherIncome()
        otherEmploymentIncomeController.getOtherEmploymentIncome(nino, taxYear)(fakeGetRequest)
      }
      status(result) mustBe NO_CONTENT
    }

    "return a 200 OK response with valid other employments income" in {

      val result = {
        mockAuth()
        mockGetValidOtherIncome()
        otherEmploymentIncomeController.getOtherEmploymentIncome(nino, taxYear)(fakeGetRequest)
      }
      status(result) mustBe OK
    }

    Seq(UNAUTHORIZED, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
      s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
        val result = {
          mockAuth()
          mockOtherIncomeFailure(httpErrorCode)
          otherEmploymentIncomeController.getOtherEmploymentIncome(nino, taxYear)(fakeGetRequest)
        }
        status(result) mustBe httpErrorCode
        contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE", "reason" -> "DES_REASON")
      }
    }

  }

}
