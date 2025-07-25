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

import models.api.PayModel
import models.shared.{Benefits, CreateUpdateEmployment}
import models.{CreateUpdateEmploymentData, CreateUpdateEmploymentRequest}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import support.helpers.{MockAuthHelper, MockEmploymentService}

class CreateUpdateEmploymentControllerSpec extends AnyWordSpec with MockAuthHelper with MockEmploymentService {

  val createEmploymentController = new CreateUpdateEmploymentController(employmentService, authorisedAction, mockControllerComponents)

  val nino = "tax_entity_id"
  val taxYear = 2020

  "createEmployment" when {

    val mtditid: String = "1234567890"
    val fakeRequest = FakeRequest("POST", "/TBC").withHeaders("mtditid" -> mtditid)

    val requestBody = CreateUpdateEmploymentRequest(
      Some("employment_id"),
      employment = Some(
        CreateUpdateEmployment(
          Some("123/12345"),
          "Misery Loves Company",
          "2020-11-11",
          None,
          None
        )
      ),
      employmentData = Some(
        CreateUpdateEmploymentData(
          PayModel(
            564563456345.55,
            34523523454.44,
            None
          ),
          None,
          benefitsInKind = Some(Benefits(
            Some(1231.33)
          )),
          Some(false)
        )
      ),
      hmrcEmploymentIdToIgnore = None
    )

    val requestBodyWithOnlyEmploymentData =requestBody.copy(employment = None)

    val invalidRequestBody =
      Json.parse(
        """
          |{
          |  "unknown_key": "value"
          |}
          |""".stripMargin)

    "request is from Individual" should {
      "return a 204 response with employmentId" in {
        val result = {
          mockAuth()
          mockAmendEmploymentSuccess()
          createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(Json.toJson(requestBody)))
        }
        status(result) mustBe NO_CONTENT
      }

      "return a 201 response with employmentId" in {
        val result = {
          mockAuth()
          mockCreateEmploymentSuccess()
          createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(Json.toJson(requestBody)))
        }
        status(result) mustBe CREATED
      }

      "return a 200 response with employmentId when request body only has mandatory values" in {
        val result = {
          mockAuth()
          mockAmendEmploymentSuccess()
          createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(Json.toJson(requestBodyWithOnlyEmploymentData)))
        }
        status(result) mustBe NO_CONTENT
      }

      "return a 400 response when request body is not valid" in {
        val result = {
          mockAuth()
          createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(invalidRequestBody))
        }
        status(result) mustBe BAD_REQUEST

      }

      Seq(UNAUTHORIZED, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuth()
            mockCreateOrAmendEmploymentFailure(httpErrorCode)
            createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(Json.toJson(requestBody)))
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
          mockAmendEmploymentSuccess()
          createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(Json.toJson(requestBody)))
        }
        status(result) mustBe NO_CONTENT
      }

      "return a 200 response with employmentId when request body only has mandatory values" in {
        val result = {
          mockAuthAsAgent()
          mockAmendEmploymentSuccess()
          createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(Json.toJson(requestBodyWithOnlyEmploymentData)))
        }
        status(result) mustBe NO_CONTENT
      }

      "return a 400 response when request body is not valid" in {
        val result = {
          mockAuthAsAgent()
          createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(invalidRequestBody))
        }
        status(result) mustBe BAD_REQUEST

      }

      Seq(UNAUTHORIZED, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorCode =>
        s"return a $httpErrorCode response when DES returns $httpErrorCode" in {
          val result = {
            mockAuthAsAgent()
            mockCreateOrAmendEmploymentFailure(httpErrorCode)
            createEmploymentController.createUpdateEmployment(nino, taxYear)(fakeRequest.withJsonBody(Json.toJson(requestBody)))
          }
          status(result) mustBe httpErrorCode
          contentAsJson(result) mustBe Json.obj("code" -> "DES_CODE" , "reason" -> "DES_REASON")
        }
      }
    }

  }
}
