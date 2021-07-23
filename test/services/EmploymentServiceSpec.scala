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

package services

import connectors.{CreateEmploymentConnector, DeleteEmploymentConnector, DeleteEmploymentFinancialDataConnector, IgnoreEmploymentConnector, UpdateEmploymentConnector}
import models.shared.{AddEmploymentResponseModel, EmploymentRequestModel}
import models.{DesErrorBodyModel, DesErrorModel}
import org.joda.time.DateTime.now
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class EmploymentServiceSpec extends TestUtils {

  private val mockCreateEmploymentConnector = mock[CreateEmploymentConnector]
  private val mockDeleteEmploymentConnector = mock[DeleteEmploymentConnector]
  private val mockDeleteEmploymentFinancialDataConnector = mock[DeleteEmploymentFinancialDataConnector]
  private val mockUpdateEmploymentDataConnector = mock[UpdateEmploymentConnector]
  private val mockIgnoreEmploymentConnector = mock[IgnoreEmploymentConnector]
  private val employmentService = new EmploymentService(mockCreateEmploymentConnector, mockDeleteEmploymentConnector,
    mockDeleteEmploymentFinancialDataConnector, mockUpdateEmploymentDataConnector,mockIgnoreEmploymentConnector)

  val nino = "entity_id"
  val taxYear = 2022
  val employmentId = "employment_id"

  "createEmployment" should {

    val addEmploymentRequestModel = EmploymentRequestModel(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))
    val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

    "return Right containing employmentId" when {

      "createEmployment connector call succeeds" in {
        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:EmploymentRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Right(addEmploymentResponseModel)))

        val result = employmentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

        await(result) mustBe Right(addEmploymentResponseModel)
      }
    }

    "return Left containing DesError" when {
      "the createEmployment connector call fails" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:EmploymentRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

        await(result) mustBe Left(desError)
      }
    }
  }

  "updateEmployment" should {

    val updateEmploymentRequestModel = EmploymentRequestModel(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))

    "return a right with no content" when {

      "updateEmployment connector call succeeds" in {
        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _:String, _:EmploymentRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, updateEmploymentRequestModel, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.updateEmployment(nino, taxYear, employmentId, updateEmploymentRequestModel)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DesError" when {
      "the updateEmployment connector call fails" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockUpdateEmploymentDataConnector.updateEmployment(_: String, _: Int, _:String, _:EmploymentRequestModel)(_: HeaderCarrier))
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

      "ALL and connectors succeed" in {
        val toRemove = "ALL"

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        (mockDeleteEmploymentConnector.deleteEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        (mockIgnoreEmploymentConnector.ignoreEmployment(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.deleteOrIgnoreEmployment(nino, employmentId, toRemove, taxYear)

        await(result) mustBe Right(())
      }
    }
  }

}
