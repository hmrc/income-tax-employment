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

import models.DES.DESEmploymentFinancialData
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalamock.handlers.CallHandler5
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
//import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.PutEmploymentFinancialDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class PutEmploymentFinancialDataControllerSpec extends TestUtils {

  val service: PutEmploymentFinancialDataService = mock[PutEmploymentFinancialDataService]
  val controller = new PutEmploymentFinancialDataController(service,mockControllerComponents,authorisedAction)
  val nino :String = "123456789"
  val employmentId :String = "123123123"
  val taxYear: Int = 1234
  val badRequestModel: DesErrorBodyModel = DesErrorBodyModel("INVALID_NINO", "Nino is invalid")
  val notFoundModel: DesErrorBodyModel = DesErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find income source")
  val serverErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVER_ERROR", "Internal server error")
  val serviceUnavailableErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable")
  private val fakePutRequest = FakeRequest("PUT", s"/income-tax/income/employments/$nino/$taxYear/$employmentId").withHeaders("MTDITID" -> "1234567890")

  val jsonBody: JsValue = Json.parse("""{"employment":{"pay":{"taxablePayToDate":0,"totalTaxToDate":-99999999999.99}}}""")


  def mockPutEmploymentFinancialDataValid(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    (service.createOrUpdateFinancialData(_: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(Right(())))
  }

  def mockPutEmploymentFinancialDataBadRequest(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    val invalidEmploymentFinancialData = Left(DesErrorModel(BAD_REQUEST, badRequestModel))
    (service.createOrUpdateFinancialData(_: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialData))
  }

  def mockPutEmploymentFinancialDataServerError(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    val invalidEmploymentFinancialData= Left(DesErrorModel(INTERNAL_SERVER_ERROR, serverErrorModel))
    (service.createOrUpdateFinancialData(_: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialData))
  }

  def mockPutEmploymentFinancialDataServiceUnavailable(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    val invalidEmploymentFinancialData = Left(DesErrorModel(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
    (service.createOrUpdateFinancialData(_: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialData))
  }

  "calling .createOrUpdateEmploymentFinancialData" should {

    "with a valid put body" should {

      "return a NO_CONTENT 204 response when called as an individual" in {
        val result = {
          mockAuth()
          mockPutEmploymentFinancialDataValid()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe NO_CONTENT
      }

      "return a NO_CONTENT 204 response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockPutEmploymentFinancialDataValid()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe NO_CONTENT
      }
    }

    "with something that causes a BAD_REQUEST" should {

      "return an BadRequest response when called as an individual" in {
        val result = {
          mockAuth()
          mockPutEmploymentFinancialDataBadRequest()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe BAD_REQUEST
      }

      "return an BadRequest response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockPutEmploymentFinancialDataBadRequest()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe BAD_REQUEST
      }
    }

    "with something that causes and internal server error in DES" should {

      "return an INTERNAL_SERVER_ERROR response when called as an individual" in {
        val result = {
          mockAuth()
          mockPutEmploymentFinancialDataServerError()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "return an INTERNAL_SERVER_ERROR response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockPutEmploymentFinancialDataServerError()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "with an unavailable service" should {

      "return a SERVICE_UNAVAILABLE response when called as an individual" in {
        val result = {
          mockAuth()
          mockPutEmploymentFinancialDataServiceUnavailable()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }

      "return a SERVICE_UNAVAILABLE response when called as an agent" in {
        val result = {
          mockAuthAsAgent()
          mockPutEmploymentFinancialDataServiceUnavailable()
          controller.createOrUpdateEmploymentFinancialData(nino, taxYear, employmentId)(fakePutRequest.withJsonBody(jsonBody))
        }
        status(result) mustBe SERVICE_UNAVAILABLE
      }
    }
  }
}
