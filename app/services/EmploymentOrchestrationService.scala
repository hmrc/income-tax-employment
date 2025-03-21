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
import connectors.errors.ApiError
import connectors.errors.SingleErrorBody.parsingError
import connectors.parsers.GetEmploymentDataHttpParser.GetEmploymentDataResponse
import connectors.parsers.GetEmploymentExpensesHttpParser.GetEmploymentExpensesResponse
import connectors.parsers.GetEmploymentListHttpParser.GetEmploymentListResponse
import models._
import models.frontend.{AllEmploymentData, EmploymentSource, HmrcEmploymentSource}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps
import utils.ViewParameterValidation.{CUSTOMER, HMRC_HELD}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmploymentOrchestrationService @Inject()(getEmploymentListConnector: GetEmploymentListConnector,
                                               getEmploymentDataConnector: GetEmploymentDataConnector,
                                               getEmploymentExpensesConnector: GetEmploymentExpensesConnector,
                                               otherEmploymentIncomeService: OtherEmploymentIncomeService) {

  def getAllEmploymentData(nino: String, taxYear: Int, mtditid: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, AllEmploymentData]] = {

    getEmploymentList(nino, taxYear).flatMap {
      case Right(Some(api.EmploymentList(hmrc, customer))) =>
        getDataAndCreateEmploymentModel(nino, taxYear, hmrc.getOrElse(Seq()), customer.getOrElse(Seq()), mtditid)
      case Right(None) => getDataAndCreateEmploymentModel(nino, taxYear, Seq(), Seq(), mtditid)
      case Left(error) => Future.successful(Left(error))
    }
  }

  private def getDataAndCreateEmploymentModel(nino: String, taxYear: Int, hmrc: Seq[api.HmrcEmployment], customer: Seq[api.CustomerEmployment],
                                              mtditid: String)
                                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, AllEmploymentData]] = {

    orchestrateHmrcEmploymentDataRetrieval(nino, taxYear, hmrc, mtditid).flatMap { hmrcResponse =>
      if (hmrcResponse.forall(_.isRight)) {
        getExpenses(nino, taxYear, HMRC_HELD, mtditid).flatMap { hmrcExpenses =>
          if (hmrcExpenses.isRight) {
            val hmrcEmployments: Seq[HmrcEmploymentSource] = hmrcResponse.collect { case Right(employment) => employment }
            orchestrateCustomerEmploymentDataRetrieval(nino, taxYear, customer, mtditid).flatMap { customerResponse => {
              if (customerResponse.forall(_.isRight)) {
                getExpenses(nino, taxYear, CUSTOMER, mtditid).flatMap { customerExpenses =>
                  if (customerExpenses.isRight) {
                    otherEmploymentIncomeService.getOtherEmploymentIncome(nino, taxYear, mtditid).map { otherEmploymentIncome =>
                      if(otherEmploymentIncome.isRight) {
                        val customerEmployments: Seq[EmploymentSource] = customerResponse.collect { case Right(employment) => employment }
                        Right(AllEmploymentData(
                          hmrcEmployments,
                          hmrcExpenses.toOption.get.map(_.toEmploymentExpenses),
                          customerEmployments,
                          customerExpenses.toOption.get.map(_.toEmploymentExpenses),
                          otherEmploymentIncome.toOption.get
                        ))
                      } else {
                        returnError(Seq(otherEmploymentIncome))
                      }
                    }
                  } else {
                    Future(returnError(Seq(customerExpenses)))
                  }
                }
              } else {
                Future(returnError(customerResponse))
              }
            }
            }
          } else {
            Future(returnError(Seq(hmrcExpenses)))
          }
        }
      } else {
        Future(returnError(hmrcResponse))
      }
    }
  }

  private[services] def returnError[T](response: Seq[Either[ApiError, T]]): Either[ApiError, AllEmploymentData] = {
    val errors: Seq[ApiError] = response.collect { case Left(errors) => errors }
    Left(errors.headOption.getOrElse(ApiError(INTERNAL_SERVER_ERROR, parsingError())))
  }

  private def orchestrateHmrcEmploymentDataRetrieval(nino: String, taxYear: Int, hmrcEmploymentData: Seq[api.HmrcEmployment], mtditid: String)
                                                    (implicit hc: HeaderCarrier,
                                                     ec: ExecutionContext): Future[Seq[Either[ApiError, HmrcEmploymentSource]]] = {
    Future.sequence(hmrcEmploymentData.map {
      hmrcEmployment =>
        val employmentId = hmrcEmployment.employmentId
        (for {
          hmrcEmploymentData <- FutureEitherOps[ApiError, Option[api.EmploymentData]](getEmploymentData(nino, taxYear, employmentId, HMRC_HELD))
          customerEmploymentData <- FutureEitherOps[ApiError, Option[api.EmploymentData]](getEmploymentData(nino, taxYear, employmentId, CUSTOMER))
        } yield {
          hmrcEmployment.toHmrcEmploymentSource(hmrcEmploymentData, customerEmploymentData)
        }).value
    })
  }

  def getEmploymentData(nino: String, taxYear: Int, employmentId: String, view: String)
                       (implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] = {
    getEmploymentDataConnector.getEmploymentData(nino, taxYear, employmentId, view)
  }

  private def orchestrateCustomerEmploymentDataRetrieval(nino: String, taxYear: Int, customerEmploymentData: Seq[api.CustomerEmployment], mtditid: String)
                                                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Either[ApiError,
    EmploymentSource]]] = {

    val view = CUSTOMER
    Future.sequence(customerEmploymentData.map {
      customerEmployment =>
        (for {
          employmentData <- FutureEitherOps[ApiError, Option[api.EmploymentData]](getEmploymentData(nino, taxYear, customerEmployment.employmentId, view))
        } yield {
          customerEmployment.toEmploymentSource(employmentData)
        }).value
    })
  }

  private def getExpenses(nino: String, taxYear: Int, view: String, mtditid: String)
                         (implicit hc: HeaderCarrier): Future[GetEmploymentExpensesResponse] = {
    getEmploymentExpensesConnector.getEmploymentExpenses(nino, taxYear, view)(hc.withExtraHeaders("mtditid" -> mtditid))
  }

  def getEmploymentList(nino: String, taxYear: Int)
                               (implicit hc: HeaderCarrier): Future[GetEmploymentListResponse] = {
    getEmploymentListConnector.getEmploymentList(nino, taxYear, None)
  }

}
