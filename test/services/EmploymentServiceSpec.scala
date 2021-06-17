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

import connectors.{CreateEmploymentConnector, DeleteEmploymentConnector, DeleteEmploymentFinancialDataConnector}
import models.shared.{AddEmploymentRequestModel, AddEmploymentResponseModel}
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
  private val employmentService = new EmploymentService(mockCreateEmploymentConnector, mockDeleteEmploymentConnector, mockDeleteEmploymentFinancialDataConnector)

  val nino = "entity_id"
  val taxYear = 2022
  val employmentId = "employment_id"

  "createEmployment" should {

    val addEmploymentRequestModel = AddEmploymentRequestModel(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))
    val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

    "return Right containing employmentId" when {

      "createEmployment connector call succeeds" in {
        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:AddEmploymentRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Right(addEmploymentResponseModel)))

        val result = employmentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

        await(result) mustBe Right(addEmploymentResponseModel)
      }
    }

    "return Left containing DesError" when {
      "the createEmployment connector call fails" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockCreateEmploymentConnector.createEmployment(_: String, _: Int, _:AddEmploymentRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

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

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancial(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Right(())))

        val result = employmentService.deleteEmploymentFinancialData(nino, taxYear, employmentId)

        await(result) mustBe Right(())
      }
    }

    "return Left containing DES Error" when {

      "the deleteEmploymentFinancialData connector fails" in {

        val desError = DesErrorModel(500, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockDeleteEmploymentFinancialDataConnector.deleteEmploymentFinancial(_: String, _: Int, _: String)(_: HeaderCarrier))
          .expects(nino, taxYear, employmentId, *)
          .returning(Future.successful(Left(desError)))

        val result = employmentService.deleteEmploymentFinancialData(nino, taxYear, employmentId)

        await(result) mustBe Left(desError)
      }
    }

  }

}
