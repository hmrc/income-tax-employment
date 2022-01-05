/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import connectors.{CreateEmploymentConnector, CreateUpdateEmploymentFinancialDataConnector, DeleteEmploymentConnector, DeleteEmploymentFinancialDataConnector, IgnoreEmploymentConnector, UpdateEmploymentConnector}
import models.DES.{DESEmploymentFinancialData, PayModel}
import models.DesErrorBodyModel.invalidCreateUpdateRequest
import models.shared.{AddEmploymentResponseModel, Benefits, CreateUpdateEmployment}
import models.{CreateUpdateEmploymentData, CreateUpdateEmploymentRequest, DesErrorBodyModel, DesErrorModel}
import org.joda.time.DateTime.now
import org.scalamock.handlers.CallHandler5
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class EmploymentServiceSpec extends TestUtils {

  private val mockCreateEmploymentConnector = mock[CreateEmploymentConnector]
  private val mockDeleteEmploymentConnector = mock[DeleteEmploymentConnector]
  private val mockDeleteEmploymentFinancialDataConnector = mock[DeleteEmploymentFinancialDataConnector]
  private val mockUpdateEmploymentDataConnector = mock[UpdateEmploymentConnector]
  private val mockIgnoreEmploymentConnector = mock[IgnoreEmploymentConnector]
  private val mockCreateUpdateEmploymentFinancialDataConnector = mock[CreateUpdateEmploymentFinancialDataConnector]
  private val employmentService = new EmploymentService(mockCreateEmploymentConnector, mockDeleteEmploymentConnector,
    mockDeleteEmploymentFinancialDataConnector, mockUpdateEmploymentDataConnector,mockIgnoreEmploymentConnector,
    mockCreateUpdateEmploymentFinancialDataConnector,mockExecutionContext)

  val nino = "entity_id"
  val taxYear = 2022
  val employmentId = "employment_id"

  val badRequestModel: DesErrorBodyModel = DesErrorBodyModel("INVALID_NINO", "Nino is invalid")
  val notFoundModel: DesErrorBodyModel = DesErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find income source")
  val serverErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVER_ERROR", "Internal server error")
  val serviceUnavailableErrorModel: DesErrorBodyModel = DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable")

  def mockPutEmploymentFinancialDataValid(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    (mockCreateUpdateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(Right(())))
  }

  def mockPutEmploymentFinancialDataBadRequest(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    val invalidEmploymentFinancialData = Left(DesErrorModel(BAD_REQUEST, badRequestModel))
    (mockCreateUpdateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialData))
  }

  def mockPutEmploymentFinancialDataServerError(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    val invalidEmploymentFinancialData= Left(DesErrorModel(INTERNAL_SERVER_ERROR, serverErrorModel))
    (mockCreateUpdateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialData))
  }

  def mockPutEmploymentFinancialDataServiceUnavailable(): CallHandler5[String, Int, String, DESEmploymentFinancialData, HeaderCarrier,
    Future[Either[DesErrorModel, Unit]]] = {
    val invalidEmploymentFinancialData = Left(DesErrorModel(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
    (mockCreateUpdateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: DESEmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialData))
  }

  val request:CreateUpdateEmploymentRequest = CreateUpdateEmploymentRequest(
    Some("employmentId"),
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
        ))
      )
    ),
    hmrcEmploymentIdToIgnore = None
  )

  "updateEmploymentCalls" should {
    "return an error if no data supplied" in {

      val result = employmentService.updateEmploymentCalls(nino, taxYear, "employmentId", None, None)

      await(result) mustBe Left(invalidCreateUpdateRequest)
    }
  }

  "createUpdateEmployment" should {
    "orchestrate the different api calls based on the model" when {
      "there is a hmrc employment to ignore" in {

        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employmentId")

        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Right(addEmploymentResponseModel)))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _:String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataValid()

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Right(Some("employmentId"))
      }
      "there is a hmrc employment to ignore but doesn't have all employment data" in {

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _:String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(
          employmentId = None, employment = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(invalidCreateUpdateRequest)
      }
      "there is a hmrc employment to ignore but the final update fails" in {

        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Right(addEmploymentResponseModel)))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _:String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataServiceUnavailable()

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(DesErrorModel(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }
      "there is a hmrc employment to ignore but the create call fails" in {

        val addEmploymentRequestModel = request.employment.get

        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Left(DesErrorModel(SERVICE_UNAVAILABLE,serviceUnavailableErrorModel))))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _:String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(DesErrorModel(SERVICE_UNAVAILABLE,serviceUnavailableErrorModel))
      }
      "there is a hmrc employment to ignore but fails to ignore" in {

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _:String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Left(DesErrorModel(SERVICE_UNAVAILABLE,serviceUnavailableErrorModel))))

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(DesErrorModel(SERVICE_UNAVAILABLE,serviceUnavailableErrorModel))
      }
      "there is a no hmrc employment to ignore" in {

        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Right(addEmploymentResponseModel)))

        mockPutEmploymentFinancialDataValid()

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None))

        await(result) mustBe Right(Some("employerId"))
      }
      "it is an update to a previously submitted customer employment" in {

        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataValid()

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId")))

        await(result) mustBe Right(None)
      }
      "it is an update to a previously submitted customer employment when the update fails" in {

        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Left(DesErrorModel(SERVICE_UNAVAILABLE,serviceUnavailableErrorModel))))

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId")))

        await(result) mustBe Left(DesErrorModel(SERVICE_UNAVAILABLE,serviceUnavailableErrorModel))
      }
      "it is an update to a previously submitted customer employment when only updating employment info" in {

        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId"),employmentData = None))

        await(result) mustBe Right(None)
      }
      "it is an update to a previously submitted customer employment when only updating employment data info" in {

        mockPutEmploymentFinancialDataValid()

        val result = employmentService.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId"),employment = None))

        await(result) mustBe Right(None)
      }
    }
  }

  "createEmployment" should {

    val addEmploymentRequestModel = CreateUpdateEmployment(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))
    val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

    "return Right containing employmentId" when {

      "createEmployment connector call succeeds" in {
        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Right(addEmploymentResponseModel)))

        val result = employmentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

        await(result) mustBe Right(addEmploymentResponseModel)
      }
    }

    "return Left containing DesError" when {
      "the createEmployment connector call fails" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

        await(result) mustBe Left(desError)
      }
    }
  }

  "updateEmployment" should {

    val updateEmploymentRequestModel = CreateUpdateEmployment(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))

    "return a right with no content" when {

      "updateEmployment connector call succeeds" in {
        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _:String, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, updateEmploymentRequestModel, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.updateEmployment(nino, taxYear, employmentId, updateEmploymentRequestModel)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DesError" when {
      "the updateEmployment connector call fails" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _:String, _:CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, updateEmploymentRequestModel, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.updateEmployment(nino, taxYear, employmentId, updateEmploymentRequestModel)

        await(result) mustBe Left(desError)
      }
    }
  }

  "deleteEmployment" should {

    "return Right" when {

      "deleteEmployment connector call succeeds" in {
        (mockDeleteEmploymentConnector.deleteEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.deleteEmployment(nino, taxYear, employmentId)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DesError" when {
      "the deleteEmployment connector call fails" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentConnector.deleteEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.deleteEmployment(nino, taxYear, employmentId)

        await(result) mustBe Left(desError)
      }
    }
  }

  "deleteEmploymentFinancialData" should {

    "return Right" when {

      "deleteEmploymentFinancialData connector call succeeds" in {

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.deleteEmploymentFinancialData(nino, taxYear, employmentId)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DES Error" when {

      "the deleteEmploymentFinancialData connector fails" in {

        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.deleteEmploymentFinancialData(nino, taxYear, employmentId)

        await(result) mustBe Left(desError)
      }
    }

  }

  "ignoreEmployment" should {

    "return Right" when {

      "ignoreEmployment connector call succeeds" in {

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.deleteEmploymentFinancialData(nino, taxYear, employmentId)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DES Error" when {

      "the ignoreEmployment connector fails" in {

        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.ignoreEmployment(nino, taxYear, employmentId)

        await(result) mustBe Left(desError)
      }
    }

  }

  "deleteOrIgnoreEmployment" should {

    "return Right" when {

      "HMRC_HELD and connectors succeed" in {

        val toRemove = "HMRC-HELD"

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Right(())
      }

      "CUSTOMER and connectors succeed" in {
        val toRemove = "CUSTOMER"

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        (mockDeleteEmploymentConnector.deleteEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Right(())
      }
    }

    "return Left" when {

      "invalid parameter" in {

        val toRemove = "HELD"
        val desError = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_TO_REMOVE_PARAMETER", "toRemove parameter is not: HMRC-HELD or CUSTOMER"))

        val result = employmentService.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }
      "HMRC_HELD and connector fails" in {

        val toRemove = "HMRC-HELD"
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }

      "CUSTOMER and first connector fails" in {
        val toRemove = "CUSTOMER"
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }

      "CUSTOMER and second connector fails" in {
        val toRemove = "CUSTOMER"
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        (mockDeleteEmploymentConnector.deleteEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }

    }
  }

}
