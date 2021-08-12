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

import connectors.{CreateEmploymentConnector, CreateUpdateEmploymentFinancialDataConnector,
  DeleteEmploymentConnector, DeleteEmploymentFinancialDataConnector, IgnoreEmploymentConnector, UpdateEmploymentConnector}
import connectors.httpParsers.CreateEmploymentHttpParser.CreateEmploymentResponse
import connectors.httpParsers.DeleteEmploymentHttpParser.DeleteEmploymentResponse
import connectors.httpParsers.DeleteEmploymentFinancialDataHttpParser.DeleteEmploymentFinancialDataResponse
import connectors.httpParsers.CreateUpdateEmploymentFinancialDataHttpParser.CreateUpdateEmploymentFinancialDataResponse
import connectors.httpParsers.UpdateEmploymentDataHttpParser.UpdateEmploymentDataResponse
import models.{CreateUpdateEmploymentData, CreateUpdateEmploymentRequest, DesErrorBodyModel, DesErrorModel}
import play.api.http.Status._
import models.shared.{AddEmploymentResponseModel, CreateUpdateEmployment}
import uk.gov.hmrc.http.HeaderCarrier
import utils.PagerDutyHelper.PagerDutyKeys.INVALID_TO_REMOVE_PARAMETER_BAD_REQUEST
import utils.ViewParameterValidation._
import utils.PagerDutyHelper.pagerDutyLog
import javax.inject.{Inject, Singleton}
import models.DES.DESEmploymentFinancialData
import models.DesErrorBodyModel.invalidCreateUpdateRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmploymentService @Inject()(createEmploymentConnector: CreateEmploymentConnector,
                                  deleteEmploymentConnector: DeleteEmploymentConnector,
                                  deleteEmploymentFinancialDataConnector: DeleteEmploymentFinancialDataConnector,
                                  updateEmploymentConnector: UpdateEmploymentConnector,
                                  ignoreEmploymentConnector: IgnoreEmploymentConnector,
                                  updateEmploymentFinancialDataConnector: CreateUpdateEmploymentFinancialDataConnector,
                                  implicit val executionContext: ExecutionContext)  {

  def createUpdateEmployment(nino: String, taxYear: Int, createUpdateEmploymentRequest: CreateUpdateEmploymentRequest)
                            (implicit hc: HeaderCarrier): Future[Either[DesErrorModel, Unit]] = {

    val hmrcEmploymentIdToIgnore = createUpdateEmploymentRequest.hmrcEmploymentIdToIgnore

    hmrcEmploymentIdToIgnore.fold{
      createUpdateEmploymentOrchestration(nino, taxYear, createUpdateEmploymentRequest)
    }{
      hmrcEmploymentIdToIgnore =>
        ignoreEmployment(nino,taxYear,hmrcEmploymentIdToIgnore).flatMap {
          case Left(error) => Future(Left(error))
          case Right(_) => createUpdateEmploymentOrchestration(nino, taxYear, createUpdateEmploymentRequest)
        }
    }
  }

  def createUpdateEmploymentOrchestration(nino: String, taxYear: Int, createUpdateEmploymentRequest: CreateUpdateEmploymentRequest)
                                         (implicit hc: HeaderCarrier): Future[Either[DesErrorModel, Unit]] = {

    createUpdateEmploymentRequest match {
      case CreateUpdateEmploymentRequest(None,Some(employment),Some(employmentData),_) =>
        createEmploymentCalls(nino,taxYear,employment,employmentData)
      case CreateUpdateEmploymentRequest(Some(employmentId),employment,employmentData,_) =>
        updateEmploymentCalls(nino,taxYear,employmentId,employment,employmentData)
      case _ => Future.successful(Left(invalidCreateUpdateRequest))
    }
  }

  def createEmploymentCalls(nino: String, taxYear: Int,
                            employment: CreateUpdateEmployment,
                            employmentData: CreateUpdateEmploymentData)
                           (implicit hc: HeaderCarrier): Future[Either[DesErrorModel, Unit]] = {

    createEmployment(nino,taxYear,employment).flatMap {
      case Left(error) => Future(Left(error))
      case Right(AddEmploymentResponseModel(employmentId)) => createOrUpdateFinancialData(nino,taxYear,employmentId,employmentData.toDESModel)
    }
  }

  def updateEmploymentCalls(nino: String, taxYear: Int,
                            employmentId: String,
                            employment: Option[CreateUpdateEmployment],
                            employmentData: Option[CreateUpdateEmploymentData])
                           (implicit hc: HeaderCarrier): Future[Either[DesErrorModel, Unit]] = {

    (employment,employmentData) match {
      case (Some(employment), Some(employmentData)) =>
        updateEmployment(nino,taxYear,employmentId,employment).flatMap {
          case Left(error) => Future(Left(error))
          case Right(_) => createOrUpdateFinancialData(nino,taxYear,employmentId,employmentData.toDESModel)
        }
      case (Some(employment), None) => updateEmployment(nino,taxYear,employmentId,employment)
      case (None, Some(employmentData)) => createOrUpdateFinancialData(nino,taxYear,employmentId,employmentData.toDESModel)
      case _ => Future.successful(Left(invalidCreateUpdateRequest))
    }
  }

  def createEmployment(nino: String, taxYear: Int, employmentModel: CreateUpdateEmployment)
                      (implicit hc: HeaderCarrier): Future[CreateEmploymentResponse] = {

    createEmploymentConnector.createEmployment(nino, taxYear, employmentModel)
  }

  def updateEmployment(nino: String, taxYear: Int, employmentId: String, employmentModel: CreateUpdateEmployment)
                      (implicit hc: HeaderCarrier): Future[UpdateEmploymentDataResponse] = {

    updateEmploymentConnector.updateEmployment(nino, taxYear, employmentId, employmentModel)
  }

  def createOrUpdateFinancialData(nino: String, taxYear: Int, employmentId:String, employmentFinancialData: DESEmploymentFinancialData)
                                 (implicit hc: HeaderCarrier): Future[CreateUpdateEmploymentFinancialDataResponse] = {
    updateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, employmentFinancialData)
  }

  def ignoreEmployment(nino: String, taxYear: Int, employmentId: String)
                      (implicit hc: HeaderCarrier): Future[DeleteEmploymentFinancialDataResponse] = {

    ignoreEmploymentConnector.ignoreEmployment(nino, taxYear, employmentId)
  }

  def deleteEmployment(nino: String, taxYear: Int, employmentId: String)
                      (implicit hc: HeaderCarrier): Future[DeleteEmploymentResponse] = {

    deleteEmploymentConnector.deleteEmployment(nino, taxYear, employmentId)
  }

  def deleteEmploymentFinancialData(nino: String, taxYear: Int, employmentId: String)
                                   (implicit hc: HeaderCarrier): Future[DeleteEmploymentFinancialDataResponse] = {

    deleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(nino, taxYear, employmentId)
  }

  def deleteOrIgnoreEmployment(nino: String, employmentId: String, toRemove: String, taxYear: Int)
                              (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[DeleteEmploymentFinancialDataResponse] = {
    toRemove match {
      case HMRC_HELD => ignoreEmployment(nino, taxYear, employmentId)
      case CUSTOMER => handleCustomerDelete(nino, employmentId, taxYear)
      case ALL => handleCustomerDelete(nino, employmentId, taxYear).flatMap {
          case Right(_) => ignoreEmployment(nino, taxYear, employmentId)
          case Left(response) => Future(Left(response))
        }
      case _ =>
        val message = "toRemove parameter is not: ALL, HMRC-HELD or CUSTOMER"
        pagerDutyLog(INVALID_TO_REMOVE_PARAMETER_BAD_REQUEST, message)
        Future(Left(DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_TO_REMOVE_PARAMETER", message))))
    }
  }

  private def handleCustomerDelete(nino: String, employmentId: String, taxYear: Int)
                            (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[DeleteEmploymentFinancialDataResponse] = {
    deleteEmploymentFinancialData(nino, taxYear, employmentId).flatMap {
      case Right(_) => deleteEmployment(nino, taxYear, employmentId).mapTo[DeleteEmploymentFinancialDataResponse]
      case Left(response) => Future(Left(response))
    }
  }

}
