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

import connectors.httpParsers.GetEmploymentBenefitsHttpParser.GetEmploymentBenefitsResponse
import connectors.httpParsers.GetEmploymentDataHttpParser.GetEmploymentDataResponse
import connectors.httpParsers.GetEmploymentExpensesHttpParser.GetEmploymentExpensesResponse
import connectors.httpParsers.GetEmploymentListHttpParser.GetEmploymentListResponse
import connectors.{GetEmploymentBenefitsConnector, GetEmploymentDataConnector, GetEmploymentExpensesConnector, GetEmploymentListConnector}
import javax.inject.Inject
import models.DES._
import models.DesErrorBodyModel.parsingError
import models.DesErrorModel
import models.frontend.{AllEmploymentData, EmploymentSource}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps
import utils.ViewParameterValidation.{CUSTOMER, HMRC_HELD}

import scala.concurrent.{ExecutionContext, Future}

class EmploymentOrchestrationService @Inject()(getEmploymentListConnector: GetEmploymentListConnector,
                                               getEmploymentDataConnector: GetEmploymentDataConnector,
                                               getEmploymentBenefitsConnector: GetEmploymentBenefitsConnector,
                                               getEmploymentExpensesConnector: GetEmploymentExpensesConnector) {

  def getAllEmploymentData(nino: String, taxYear: Int, mtditid: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllEmploymentData]] = {

    getEmploymentList(nino, taxYear).flatMap {
      case Right(Some(DESEmploymentList(hmrc, customer))) => getDataAndCreateEmploymentModel(
        nino, taxYear, hmrc.getOrElse(Seq()), customer.getOrElse(Seq()), mtditid
      )
      case Right(None) => Future(Right(AllEmploymentData(Seq.empty, None, Seq.empty, None)))
      case Left(error) => Future.successful(Left(error))
    }
  }

  private def getDataAndCreateEmploymentModel(nino: String, taxYear: Int, hmrc: Seq[HmrcEmployment], customer: Seq[CustomerEmployment],
                                              mtditid: String)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllEmploymentData]] = {

    orchestrateHmrcEmploymentDataRetrieval(nino, taxYear, hmrc, mtditid).flatMap { hmrcResponse =>
      if (hmrcResponse.forall(_.isRight)) {
        getExpenses(nino, taxYear, HMRC_HELD, mtditid).flatMap { hmrcExpenses =>
          if (hmrcExpenses.isRight) {
            val hmrcEmployments: Seq[EmploymentSource] = hmrcResponse.collect { case Right(employment) => employment }
            orchestrateCustomerEmploymentDataRetrieval(nino, taxYear, customer, mtditid).flatMap { customerResponse => {
              if (customerResponse.forall(_.isRight)) {
                getExpenses(nino, taxYear, CUSTOMER, mtditid).map { customerExpenses =>
                  if (customerExpenses.isRight) {
                    val customerEmployments: Seq[EmploymentSource] = customerResponse.collect { case Right(employment) => employment }
                    Right(AllEmploymentData(
                      hmrcEmployments,
                      hmrcExpenses.right.get.map(_.toEmploymentExpenses),
                      customerEmployments,
                      customerExpenses.right.get.map(_.toEmploymentExpenses)
                    ))
                  } else {
                    returnError(Seq(customerExpenses))
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

  private[services] def returnError[T](response: Seq[Either[DesErrorModel, T]]): Either[DesErrorModel, AllEmploymentData] = {
    val errors: Seq[DesErrorModel] = response.collect { case Left(errors) => errors }
    Left(errors.headOption.getOrElse(DesErrorModel(INTERNAL_SERVER_ERROR, parsingError())))
  }

  private def orchestrateHmrcEmploymentDataRetrieval(nino: String, taxYear: Int, hmrcEmploymentData: Seq[HmrcEmployment], mtditid: String)
                                                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Either[DesErrorModel, EmploymentSource]]] = {
    val view = HMRC_HELD
    Future.sequence(hmrcEmploymentData.map {
      hmrcEmployment =>
        (for {
          employmentData <- FutureEitherOps[DesErrorModel, Option[DESEmploymentData]](getEmploymentData(nino, taxYear, hmrcEmployment.employmentId, view))
          benefits <- FutureEitherOps[DesErrorModel, Option[DESEmploymentBenefits]](getBenefits(nino, taxYear, hmrcEmployment.employmentId, view, mtditid))
        } yield {
          hmrcEmployment.toEmploymentSource(employmentData, benefits)
        }).value
    })
  }

  def getEmploymentData(nino: String, taxYear: Int, employmentId: String, view: String)
                       (implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] = {
    getEmploymentDataConnector.getEmploymentData(nino, taxYear, employmentId, view)
  }

  private def getBenefits(nino: String, taxYear: Int, employmentId: String, view: String, mtditid: String)
                         (implicit hc: HeaderCarrier): Future[GetEmploymentBenefitsResponse] = {
    getEmploymentBenefitsConnector.getEmploymentBenefits(nino, taxYear, employmentId, view)(hc.withExtraHeaders("mtditid" -> mtditid))
  }

  private def orchestrateCustomerEmploymentDataRetrieval(nino: String, taxYear: Int, customerEmploymentData: Seq[CustomerEmployment], mtditid: String)
                                                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Either[DesErrorModel,
    EmploymentSource]]] = {

    val view = CUSTOMER
    Future.sequence(customerEmploymentData.map {
      customerEmployment =>
        (for {
          employmentData <- FutureEitherOps[DesErrorModel, Option[DESEmploymentData]](getEmploymentData(nino, taxYear, customerEmployment.employmentId, view))
          benefits <- FutureEitherOps[DesErrorModel, Option[DESEmploymentBenefits]](getBenefits(nino, taxYear, customerEmployment.employmentId, view, mtditid))
        } yield {
          customerEmployment.toEmploymentSource(employmentData, benefits)
        }).value
    })
  }

  private def getExpenses(nino: String, taxYear: Int, view: String, mtditid: String)
                         (implicit hc: HeaderCarrier): Future[GetEmploymentExpensesResponse] = {
    getEmploymentExpensesConnector.getEmploymentExpenses(nino, taxYear, view)(hc.withExtraHeaders("mtditid" -> mtditid))
  }

  private def getEmploymentList(nino: String, taxYear: Int)
                               (implicit hc: HeaderCarrier): Future[GetEmploymentListResponse] = {
    getEmploymentListConnector.getEmploymentList(nino, taxYear, None)
  }

}
