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

package services

import connectors._
import connectors.errors.SingleErrorBody.invalidCreateUpdateRequest
import connectors.errors.{ApiError, SingleErrorBody}
import connectors.parsers.CreateEmploymentHttpParser.CreateEmploymentResponse
import connectors.parsers.UnignoreEmploymentHttpParser.UnignoreEmploymentResponse
import models.api.{EmploymentFinancialData, PayModel}
import models.shared.{AddEmploymentResponseModel, Benefits, CreateUpdateEmployment}
import models.{CreateUpdateEmploymentData, CreateUpdateEmploymentRequest}
import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EmploymentServiceSpec extends AnyWordSpec with MockFactory {

  private val mockCreateEmploymentConnector = mock[CreateEmploymentConnector]
  private val mockDeleteEmploymentConnector = mock[DeleteEmploymentConnector]
  private val mockDeleteEmploymentFinancialDataConnector = mock[DeleteEmploymentFinancialDataConnector]
  private val mockUpdateEmploymentDataConnector = mock[UpdateEmploymentConnector]
  private val mockIgnoreEmploymentConnector = mock[IgnoreEmploymentConnector]
  private val mockUnignoreEmploymentConnector = mock[UnignoreEmploymentConnector]
  private val mockCreateUpdateEmploymentFinancialDataConnector = mock[CreateUpdateEmploymentFinancialDataConnector]
  private val mockCreateUpdateEmploymentFinancialDataTYSConnector = mock[CreateUpdateEmploymentFinancialDataTYSConnector]
  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  private val underTest = new EmploymentService(mockCreateEmploymentConnector,
    mockDeleteEmploymentConnector,
    mockDeleteEmploymentFinancialDataConnector,
    mockUpdateEmploymentDataConnector,
    mockIgnoreEmploymentConnector,
    mockUnignoreEmploymentConnector,
    mockCreateUpdateEmploymentFinancialDataConnector,
    mockCreateUpdateEmploymentFinancialDataTYSConnector,
    ExecutionContext.Implicits.global)

  private val nino = "entity_id"
  private val taxYear = 2022
  private val specificTaxYear = 2024
  private val employmentId = "employment_id"

  private val serviceUnavailableErrorModel: SingleErrorBody = SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable")

  private def mockPutEmploymentFinancialDataValid(): CallHandler5[String, Int, String, EmploymentFinancialData, HeaderCarrier,
    Future[Either[ApiError, Unit]]] = {
    (mockCreateUpdateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: EmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(Right(())))
  }

  private def mockPutEmploymentFinancialDataTYSValid(): CallHandler5[String, Int, String, EmploymentFinancialData, HeaderCarrier,
    Future[Either[ApiError, Unit]]] = {
    (mockCreateUpdateEmploymentFinancialDataTYSConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: EmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(Right(())))
  }
  private def mockCreateEmployment(nino: String,
                                   taxYear: Int,
                                   createUpdateEmployment: CreateUpdateEmployment,
                                   connectorResult: CreateEmploymentResponse) = {
    (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _: CreateUpdateEmployment)(_: HeaderCarrier))
      .expects(nino, taxYear, createUpdateEmployment, *)
      .returning(Future.successful(connectorResult))
  }

  private def mockUnignoreEmployment(nino: String, taxYear: Int, employmentId: String, connectorResult: UnignoreEmploymentResponse) = {
    (mockUnignoreEmploymentConnector.unignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(nino, taxYear, employmentId, *)
      .returning(Future.successful(connectorResult))
  }

  private def mockPutEmploymentFinancialDataServiceUnavailable(): CallHandler5[String, Int, String, EmploymentFinancialData, HeaderCarrier,
    Future[Either[ApiError, Unit]]] = {
    val invalidEmploymentFinancialData = Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
    (mockCreateUpdateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: EmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialData))
  }

  private def mockPutEmploymentFinancialDataTYSServiceUnavailable(): CallHandler5[String, Int, String, EmploymentFinancialData, HeaderCarrier,
    Future[Either[ApiError, Unit]]] = {
    val invalidEmploymentFinancialDataTYS = Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
    (mockCreateUpdateEmploymentFinancialDataTYSConnector.createUpdateEmploymentFinancialData(
      _: String, _: Int, _: String, _: EmploymentFinancialData)(_: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(invalidEmploymentFinancialDataTYS))
  }

  private val createUpdateEmploymentData: CreateUpdateEmploymentData = CreateUpdateEmploymentData(
    PayModel(564563456345.55, 34523523454.44, None),
    None,
    benefitsInKind = Some(Benefits(Some(1231.33))),
    None
  )

  private val createUpdateEmployment: CreateUpdateEmployment = CreateUpdateEmployment(
    Some("123/12345"),
    "Misery Loves Company",
    "2020-11-11",
    None,
    None
  )

  private val request: CreateUpdateEmploymentRequest = CreateUpdateEmploymentRequest(
    Some("employmentId"),
    employment = Some(createUpdateEmployment),
    employmentData = Some(createUpdateEmploymentData),
    hmrcEmploymentIdToIgnore = None
  )

  "updateEmploymentCalls" should {
    "return an error if no data supplied" in {
      val result = underTest.updateEmploymentCalls(nino, taxYear, "employmentId", None, None)

      await(result) mustBe Left(invalidCreateUpdateRequest)
    }
  }

  "updateEmploymentCalls for specific tax year" should {
    "return an error if no data supplied" in {
      val result = underTest.updateEmploymentCalls(nino, specificTaxYear, "employmentId", None, None)

      await(result) mustBe Left(invalidCreateUpdateRequest)
    }
  }

  ".unignoreEmployment" should {
    "return the connector response" in {
      mockUnignoreEmployment(nino, taxYear, "employmentId", Right(()))

      val result = underTest.unignoreEmployment(nino, taxYear, "employmentId")

      await(result) mustBe Right(())
    }
  }

  "createUpdateEmployment" should {
    "orchestrate the different api calls based on the model" when {
      "the employment id is a hmrc employment id" in {

        mockPutEmploymentFinancialDataValid()

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employment = None, isHmrcEmploymentId = Some(true)))

        await(result) mustBe Right(None)
      }

      "the employment id is a hmrc employment id but it fails" in {

        mockPutEmploymentFinancialDataServiceUnavailable()

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employment = None, isHmrcEmploymentId = Some(true)))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "there is a hmrc employment to ignore" in {
        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employmentId")

        mockCreateEmployment(nino, taxYear, addEmploymentRequestModel, Right(addEmploymentResponseModel))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataValid()

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Right(Some("employmentId"))
      }

      "there is a hmrc employment to ignore but doesn't have all employment data" in {
        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(
          employmentId = None, employment = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(invalidCreateUpdateRequest)
      }

      "there is a hmrc employment to ignore but the final update fails" in {
        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

        mockCreateEmployment(nino, taxYear, addEmploymentRequestModel, Right(addEmploymentResponseModel))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataServiceUnavailable()

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(ApiError(INTERNAL_SERVER_ERROR, serviceUnavailableErrorModel))
      }

      "there is a hmrc employment to ignore but the create call fails" in {
        val addEmploymentRequestModel = request.employment.get

        mockCreateEmployment(nino, taxYear, addEmploymentRequestModel, Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel)))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "there is a hmrc employment to ignore but fails to ignore" in {
        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", *)
          .returning(Future.successful(Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))))

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "there is a no hmrc employment to ignore" in {
        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

        mockCreateEmployment(nino, taxYear, addEmploymentRequestModel, Right(addEmploymentResponseModel))
        mockPutEmploymentFinancialDataValid()

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = None))

        await(result) mustBe Right(Some("employerId"))
      }

      "it is an update to a previously submitted customer employment" in {
        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataValid()

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId")))

        await(result) mustBe Right(None)
      }

      "it is an update to a previously submitted customer employment when the update fails" in {
        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))))

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId")))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "it is an update to a previously submitted customer employment when only updating employment info" in {
        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Right(())))

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId"), employmentData = None))

        await(result) mustBe Right(None)
      }

      "it is an update to a previously submitted customer employment when only updating employment data info" in {
        mockPutEmploymentFinancialDataValid()

        val result = underTest.createUpdateEmployment(nino, taxYear, request.copy(employmentId = Some("employmentId"), employment = None))

        await(result) mustBe Right(None)
      }
    }
  }

  "createUpdateEmployment for Specific Tax Year" should {
    "orchestrate the different api calls based on the model" when {
      "the employment id is a hmrc employment id" in {

        mockPutEmploymentFinancialDataTYSValid()

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employment = None, isHmrcEmploymentId = Some(true)))

        await(result) mustBe Right(None)
      }

      "the employment id is a hmrc employment id but it fails" in {

        mockPutEmploymentFinancialDataTYSServiceUnavailable()

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employment = None, isHmrcEmploymentId = Some(true)))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "there is a hmrc employment to ignore" in {
        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employmentId")

        mockCreateEmployment(nino, specificTaxYear, addEmploymentRequestModel, Right(addEmploymentResponseModel))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataTYSValid()

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Right(Some("employmentId"))
      }

      "there is a hmrc employment to ignore but doesn't have all employment data" in {
        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(
          employmentId = None, employment = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(invalidCreateUpdateRequest)
      }

      "there is a hmrc employment to ignore but the final update fails" in {
        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

        mockCreateEmployment(nino, specificTaxYear, addEmploymentRequestModel, Right(addEmploymentResponseModel))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataTYSServiceUnavailable()

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(ApiError(INTERNAL_SERVER_ERROR, serviceUnavailableErrorModel))
      }

      "there is a hmrc employment to ignore but the create call fails" in {
        val addEmploymentRequestModel = request.employment.get

        mockCreateEmployment(nino, specificTaxYear, addEmploymentRequestModel, Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel)))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", *)
          .returning(Future.successful(Right(())))

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "there is a hmrc employment to ignore but fails to ignore" in {
        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", *)
          .returning(Future.successful(Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))))

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = None, hmrcEmploymentIdToIgnore = Some("employmentId")))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "there is a no hmrc employment to ignore" in {
        val addEmploymentRequestModel = request.employment.get
        val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

        mockCreateEmployment(nino, specificTaxYear, addEmploymentRequestModel, Right(addEmploymentResponseModel))
        mockPutEmploymentFinancialDataTYSValid()

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = None))

        await(result) mustBe Right(Some("employerId"))
      }

      "it is an update to a previously submitted customer employment" in {
        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Right(())))

        mockPutEmploymentFinancialDataTYSValid()

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = Some("employmentId")))

        await(result) mustBe Right(None)
      }

      "it is an update to a previously submitted customer employment when the update fails" in {
        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))))

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = Some("employmentId")))

        await(result) mustBe Left(ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel))
      }

      "it is an update to a previously submitted customer employment when only updating employment info" in {
        val employmentRequestModel = request.employment.get

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, specificTaxYear, "employmentId", employmentRequestModel, *)
          .returning(Future.successful(Right(())))

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = Some("employmentId"), employmentData = None))

        await(result) mustBe Right(None)
      }

      "it is an update to a previously submitted customer employment when only updating employment data info" in {
        mockPutEmploymentFinancialDataTYSValid()

        val result = underTest.createUpdateEmployment(nino, specificTaxYear, request.copy(employmentId = Some("employmentId"), employment = None))

        await(result) mustBe Right(None)
      }
    }
  }

  "createEmployment" should {
    val addEmploymentRequestModel = CreateUpdateEmployment(Some("employerRef"), "employerName", LocalDateTime.now().toString, Some(LocalDateTime.now().toString), Some("payrollId"))
    val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

    "return Right containing employmentId" when {

      "createEmployment connector call succeeds" in {
        mockCreateEmployment(nino, taxYear, addEmploymentRequestModel, Right(addEmploymentResponseModel))

        await(underTest.createEmployment(nino, taxYear, addEmploymentRequestModel)) mustBe Right(addEmploymentResponseModel)
      }
    }

    "return Left containing DesError" when {
      "the createEmployment connector call fails" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        mockCreateEmployment(nino, taxYear, addEmploymentRequestModel, Left(desError))

        await(underTest.createEmployment(nino, taxYear, addEmploymentRequestModel)) mustBe Left(desError)
      }
    }
  }

  "updateEmployment" should {

    val updateEmploymentRequestModel = CreateUpdateEmployment(Some("employerRef"), "employerName", LocalDateTime.now().toString, Some(LocalDateTime.now().toString), Some("payrollId"))

    "return a right with no content" when {

      "updateEmployment connector call succeeds" in {
        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, updateEmploymentRequestModel, *)
          .returning(Future.successful(Right(())))

        val result = underTest.updateEmployment(nino, taxYear, employmentId, updateEmploymentRequestModel)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DesError" when {
      "the updateEmployment connector call fails" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _: String, _: CreateUpdateEmployment)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, updateEmploymentRequestModel, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.updateEmployment(nino, taxYear, employmentId, updateEmploymentRequestModel)

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

        val result = underTest.deleteEmployment(nino, taxYear, employmentId)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DesError" when {
      "the deleteEmployment connector call fails" in {
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentConnector.deleteEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.deleteEmployment(nino, taxYear, employmentId)

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

        val result = underTest.deleteEmploymentFinancialData(nino, taxYear, employmentId)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DES Error" when {

      "the deleteEmploymentFinancialData connector fails" in {

        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.deleteEmploymentFinancialData(nino, taxYear, employmentId)

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

        val result = underTest.deleteEmploymentFinancialData(nino, taxYear, employmentId)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DES Error" when {

      "the ignoreEmployment connector fails" in {

        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.ignoreEmployment(nino, taxYear, employmentId)

        await(result) mustBe Left(desError)
      }
    }

  }

  "deleteOrIgnoreEmployment" should {

    "return Right" when {

      "ALL and connectors succeed" in {

        val toRemove = "ALL"

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Right(())
      }

      "HMRC_HELD and connectors succeed" in {

        val toRemove = "HMRC-HELD"

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

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

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Right(())
      }
    }

    "return Left" when {

      "ALL and delete connector fails" in {

        val toRemove = "ALL"
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }
      "ALL and ignore connector fails" in {

        val toRemove = "ALL"
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }

      "invalid parameter" in {

        val toRemove = "HELD"
        val desError = ApiError(BAD_REQUEST, SingleErrorBody("INVALID_TO_REMOVE_PARAMETER", "toRemove parameter is not: HMRC-HELD or CUSTOMER"))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }
      "HMRC_HELD and connector fails" in {

        val toRemove = "HMRC-HELD"
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }

      "CUSTOMER and first connector fails" in {
        val toRemove = "CUSTOMER"
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }

      "CUSTOMER and second connector fails" in {
        val toRemove = "CUSTOMER"
        val desError = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        (mockDeleteEmploymentConnector.deleteEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = underTest.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Left(desError)
      }

    }
  }
}
