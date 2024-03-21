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

import cats.data.EitherT
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import connectors._
import connectors.errors.SingleErrorBody.invalidCreateUpdateRequest
import connectors.errors.{ApiError, SingleErrorBody}
import connectors.parsers.CreateUpdateEmploymentFinancialDataHttpParser.CreateUpdateEmploymentFinancialDataResponse
import connectors.parsers.DeleteEmploymentFinancialDataHttpParser.DeleteEmploymentFinancialDataResponse
import connectors.parsers.DeleteEmploymentHttpParser.DeleteEmploymentResponse
import connectors.parsers.IgnoreEmploymentHttpParser.IgnoreEmploymentResponse
import connectors.parsers.UnignoreEmploymentHttpParser.UnignoreEmploymentResponse
import connectors.parsers.UpdateEmploymentDataHttpParser.UpdateEmploymentDataResponse
import models.api.EmploymentFinancialData
import models.shared.{AddEmploymentResponseModel, CreateUpdateEmployment}
import models.{CreateUpdateEmploymentData, CreateUpdateEmploymentRequest}
import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import utils.PagerDutyHelper.PagerDutyKeys.INVALID_TO_REMOVE_PARAMETER_BAD_REQUEST
import utils.PagerDutyHelper.pagerDutyLog
import utils.TaxYearUtils.specificTaxYear
import utils.ViewParameterValidation._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmploymentService @Inject() (createEmploymentConnector: CreateEmploymentConnector,
                                   deleteEmploymentConnector: DeleteEmploymentConnector,
                                   deleteEmploymentFinancialDataConnector: DeleteEmploymentFinancialDataConnector,
                                   updateEmploymentConnector: UpdateEmploymentConnector,
                                   ignoreEmploymentConnector: IgnoreEmploymentConnector,
                                   unignoreEmploymentConnector: UnignoreEmploymentConnector,
                                   updateEmploymentFinancialDataConnector: CreateUpdateEmploymentFinancialDataConnector,
                                   updateEmploymentFinancialDataTYSConnector: CreateUpdateEmploymentFinancialDataTYSConnector,
                                   submissionsConnector: SubmissionConnector,
                                   implicit val executionContext: ExecutionContext) {

  def createUpdateEmployment(nino: String, taxYear: Int, mtdItId: String, createUpdateEmploymentRequest: CreateUpdateEmploymentRequest)(implicit
      hc: HeaderCarrier): Future[Either[ApiError, Option[String]]] = {

    val hmrcEmploymentIdToIgnore = createUpdateEmploymentRequest.hmrcEmploymentIdToIgnore

    hmrcEmploymentIdToIgnore.fold {
      createUpdateEmploymentOrchestration(nino, taxYear, mtdItId, createUpdateEmploymentRequest)
    } { hmrcEmploymentIdToIgnore =>
      ignoreEmployment(nino, taxYear, hmrcEmploymentIdToIgnore).flatMap {
        case Left(error) => Future(Left(error))
        case Right(_)    => createUpdateEmploymentOrchestration(nino, taxYear, mtdItId, createUpdateEmploymentRequest)
      }
    }
  }

  def createUpdateEmploymentOrchestration(nino: String, taxYear: Int, mtdItId: String, createUpdateEmploymentRequest: CreateUpdateEmploymentRequest)(
      implicit hc: HeaderCarrier): Future[Either[ApiError, Option[String]]] =
    createUpdateEmploymentRequest match {
      case CreateUpdateEmploymentRequest(Some(hmrcEmploymentId), _, Some(employmentData), None, Some(true)) =>
        updateEmploymentCalls(nino, taxYear, mtdItId, hmrcEmploymentId, None, Some(employmentData)).map {
          case Left(error) => Left(error)
          case Right(_)    => Right(None)
        }

      case CreateUpdateEmploymentRequest(None, Some(employment), Some(employmentData), _, _) =>
        createEmploymentCalls(nino, taxYear, mtdItId, employment, employmentData)

      case CreateUpdateEmploymentRequest(Some(employmentId), employment, employmentData, _, _) =>
        updateEmploymentCalls(nino, taxYear, mtdItId, employmentId, employment, employmentData).map {
          case Left(error) => Left(error)
          case Right(_)    => Right(None)
        }
      case _ => Future.successful(Left(invalidCreateUpdateRequest))
    }

  private def createEmploymentCalls(nino: String,
                                    taxYear: Int,
                                    mtdItId: String,
                                    employment: CreateUpdateEmployment,
                                    employmentData: CreateUpdateEmploymentData)(implicit
      hc: HeaderCarrier): Future[Either[ApiError, Option[String]]] =
    (for {
      resp <- EitherT(createEmploymentConnector.createEmployment(nino, taxYear, employment))
      _    <- EitherT(createOrUpdateFinancialData(nino, taxYear, resp.employmentId, employmentData.toDESModel))
      _    <- EitherT(submissionsConnector.refreshSubmissionCache(nino, taxYear, mtdItId))
    } yield resp.employmentId.some).leftMap {
      case error @ ApiError(SERVICE_UNAVAILABLE, _) => error.copy(status = INTERNAL_SERVER_ERROR)
      case error                                    => error
    }.value

  def updateEmploymentCalls(nino: String,
                            taxYear: Int,
                            mtdItId: String,
                            employmentId: String,
                            employment: Option[CreateUpdateEmployment],
                            employmentData: Option[CreateUpdateEmploymentData])(implicit hc: HeaderCarrier): Future[Either[ApiError, Unit]] =
    (employment, employmentData) match {
      case (Some(employment), Some(employmentData)) =>
        (for {
          _ <- EitherT(updateEmployment(nino, taxYear, employmentId, employment))
          _ <- EitherT(createOrUpdateFinancialData(nino, taxYear, employmentId, employmentData.toDESModel))
          _ <- EitherT(submissionsConnector.refreshSubmissionCache(nino, taxYear, mtdItId))
        } yield ()).value

      case (Some(employment), None) =>
        (for {
          _ <- EitherT(updateEmployment(nino, taxYear, employmentId, employment))
          _ <- EitherT(submissionsConnector.refreshSubmissionCache(nino, taxYear, mtdItId))
        } yield ()).value

      case (None, Some(employmentData)) =>
        (for {
          _ <- EitherT(createOrUpdateFinancialData(nino, taxYear, employmentId, employmentData.toDESModel))
          _ <- EitherT(submissionsConnector.refreshSubmissionCache(nino, taxYear, mtdItId))
        } yield ()).value

      case _ =>
        Future.successful(invalidCreateUpdateRequest.asLeft)
    }

  def createEmployment(nino: String, taxYear: Int, employmentModel: CreateUpdateEmployment)(implicit
      hc: HeaderCarrier): DownstreamOutcome[AddEmploymentResponseModel] =
    createEmploymentConnector.createEmployment(nino, taxYear, employmentModel)

  def updateEmployment(nino: String, taxYear: Int, employmentId: String, employmentModel: CreateUpdateEmployment)(implicit
      hc: HeaderCarrier): Future[UpdateEmploymentDataResponse] =
    updateEmploymentConnector.updateEmployment(nino, taxYear, employmentId, employmentModel)

  def createOrUpdateFinancialData(nino: String, taxYear: Int, employmentId: String, employmentFinancialData: EmploymentFinancialData)(implicit
      hc: HeaderCarrier): Future[CreateUpdateEmploymentFinancialDataResponse] =
    if (taxYear >= specificTaxYear) {
      updateEmploymentFinancialDataTYSConnector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, employmentFinancialData)
    } else {
      updateEmploymentFinancialDataConnector.createUpdateEmploymentFinancialData(nino, taxYear, employmentId, employmentFinancialData)
    }

  def ignoreEmployment(nino: String, taxYear: Int, employmentId: String)(implicit hc: HeaderCarrier): Future[IgnoreEmploymentResponse] =
    ignoreEmploymentConnector.ignoreEmployment(nino, taxYear, employmentId)

  def unignoreEmployment(nino: String, taxYear: Int, employmentId: String)(implicit hc: HeaderCarrier): Future[UnignoreEmploymentResponse] =
    unignoreEmploymentConnector.unignoreEmployment(nino, taxYear, employmentId)

  def deleteEmployment(nino: String, taxYear: Int, employmentId: String)(implicit hc: HeaderCarrier): Future[DeleteEmploymentResponse] =
    deleteEmploymentConnector.deleteEmployment(nino, taxYear, employmentId)

  def deleteEmploymentFinancialData(nino: String, taxYear: Int, employmentId: String)(implicit
      hc: HeaderCarrier): Future[DeleteEmploymentFinancialDataResponse] =
    deleteEmploymentFinancialDataConnector.deleteEmploymentFinancialData(nino, taxYear, employmentId)

  def deleteOrIgnoreEmployment(nino: String, employmentId: String, toRemove: String, taxYear: Int, mtdItId: String)(implicit
      hc: HeaderCarrier,
      executionContext: ExecutionContext): Future[DeleteEmploymentFinancialDataResponse] =
    toRemove match {
      case ALL       => handleDeleteAllHmrc(nino, taxYear, employmentId)
      case HMRC_HELD => ignoreEmployment(nino, taxYear, employmentId)
      case CUSTOMER  => handleCustomerDelete(nino, taxYear, employmentId, mtdItId)
      case _ =>
        val message = "toRemove parameter is not: HMRC-HELD or CUSTOMER"
        pagerDutyLog(INVALID_TO_REMOVE_PARAMETER_BAD_REQUEST, message)
        Future(Left(ApiError(BAD_REQUEST, SingleErrorBody("INVALID_TO_REMOVE_PARAMETER", message))))
    }

  private def handleDeleteAllHmrc(nino: String, taxYear: Int, employmentId: String)(implicit
      hc: HeaderCarrier,
      executionContext: ExecutionContext): Future[DeleteEmploymentFinancialDataResponse] =
    deleteEmploymentFinancialData(nino, taxYear, employmentId).flatMap {
      case Right(_)       => ignoreEmployment(nino, taxYear, employmentId).mapTo[DeleteEmploymentFinancialDataResponse]
      case Left(response) => Future(Left(response))
    }

  private def handleCustomerDelete(nino: String, taxYear: Int, employmentId: String, mtdItId: String)(implicit
      hc: HeaderCarrier): Future[DeleteEmploymentFinancialDataResponse] =
    (for {
      _ <- EitherT(deleteEmploymentFinancialData(nino, taxYear, employmentId))
      _ <- EitherT(deleteEmployment(nino, taxYear, employmentId).mapTo[DeleteEmploymentFinancialDataResponse])
      _ <- EitherT(submissionsConnector.refreshSubmissionCache(nino, taxYear, mtdItId))
    } yield ()).value

}
