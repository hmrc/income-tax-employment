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

import connectors.httpParsers.GetEmploymentBenefitsHttpParser.GetEmploymentBenefitsResponse
import connectors.{GetEmploymentBenefitsConnector, GetEmploymentDataConnector, GetEmploymentExpensesConnector, GetEmploymentListConnector}
import connectors.httpParsers.GetEmploymentDataHttpParser.GetEmploymentDataResponse
import connectors.httpParsers.GetEmploymentExpensesHttpParser.GetEmploymentExpensesResponse
import connectors.httpParsers.GetEmploymentListHttpParser.GetEmploymentListResponse
import uk.gov.hmrc.http.HeaderCarrier
import javax.inject.Inject
import models.DES.{CustomerEmployment, DESEmploymentBenefits, DESEmploymentData, DESEmploymentExpenses, DESEmploymentList, HmrcEmployment}
import models.DesErrorBodyModel.parsingError
import models.frontend.{AllEmploymentData, EmploymentSource}
import models.DesErrorModel
import utils.ViewParameterValidation.{CUSTOMER, HMRC_HELD}
import utils.FutureEitherOps
import play.api.http.Status.INTERNAL_SERVER_ERROR

import scala.concurrent.{ExecutionContext, Future}

class EmploymentOrchestrationService @Inject()(getEmploymentListConnector: GetEmploymentListConnector,
                                               getEmploymentDataConnector: GetEmploymentDataConnector,
                                               getEmploymentBenefitsConnector: GetEmploymentBenefitsConnector,
                                               getEmploymentExpensesConnector: GetEmploymentExpensesConnector) {

  def getAllEmploymentData(nino: String, taxYear: Int)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllEmploymentData]] = {

    getEmploymentList(nino, taxYear).flatMap {
      case Right(Some(DESEmploymentList(hmrc,customer))) => getDataAndFormEmploymentModel(nino, taxYear, hmrc, customer)
      case Right(None) => Future(Right(AllEmploymentData(Seq.empty,Seq.empty)))
      case Left(error) => Future.successful(Left(error))
    }
  }

  private def getDataAndFormEmploymentModel(nino: String, taxYear: Int, hmrc: Seq[HmrcEmployment], customer: Seq[CustomerEmployment])
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllEmploymentData]] ={

    orchestrateHmrcEmploymentDataRetrieval(nino, taxYear, hmrc).flatMap{
      hmrcResponse =>
        if(hmrcResponse.forall(_.isRight)){

          val hmrcEmployments: Seq[EmploymentSource] = hmrcResponse.collect{ case Right(employment) => employment }
          orchestrateCustomerEmploymentDataRetrieval(nino, taxYear, customer).map(
            customerResponse => {
              if(customerResponse.forall(_.isRight)){

                val customerEmployments: Seq[EmploymentSource] = customerResponse.collect{ case Right(employment) => employment }
                Right(AllEmploymentData(hmrcEmployments,customerEmployments))
              } else {
                returnError(customerResponse)
              }
            }
          )
        } else {
          Future(returnError(hmrcResponse))
        }
    }
  }

  private def returnError(response: Seq[Either[DesErrorModel,EmploymentSource]]): Either[DesErrorModel, AllEmploymentData] ={
    val errors: Seq[DesErrorModel] = response.collect{ case Left(errors) => errors }
    Left(errors.headOption.getOrElse(DesErrorModel(INTERNAL_SERVER_ERROR, parsingError)))
  }

  private def orchestrateHmrcEmploymentDataRetrieval(nino: String, taxYear: Int, hmrcEmploymentData: Seq[HmrcEmployment])
                                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Either[DesErrorModel,EmploymentSource]]] ={
    val view = HMRC_HELD
    Future.sequence(hmrcEmploymentData.map{
      hmrcEmployment =>
        (for {
          employmentData <- FutureEitherOps[DesErrorModel, Option[DESEmploymentData]](getEmploymentData(nino,taxYear,hmrcEmployment.employmentId,view))
          expenses <- FutureEitherOps[DesErrorModel, Option[DESEmploymentExpenses]](getExpenses(nino,taxYear,view))
          benefits <- FutureEitherOps[DesErrorModel, Option[DESEmploymentBenefits]](getBenefits(nino,taxYear,hmrcEmployment.employmentId,view))
        } yield {
          hmrcEmployment.toEmploymentSource(employmentData, expenses, benefits)
        }).value
    })
  }

  private def orchestrateCustomerEmploymentDataRetrieval[E](nino: String, taxYear: Int, customerEmploymentData: Seq[CustomerEmployment])
                                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Either[DesErrorModel, EmploymentSource]]] = {
    val view = CUSTOMER
    Future.sequence(customerEmploymentData.map{
      customerEmployment =>
        (for {
          employmentData <- FutureEitherOps[DesErrorModel, Option[DESEmploymentData]](getEmploymentData(nino,taxYear,customerEmployment.employmentId,view))
          expenses <- FutureEitherOps[DesErrorModel, Option[DESEmploymentExpenses]](getExpenses(nino,taxYear,view))
          benefits <- FutureEitherOps[DesErrorModel, Option[DESEmploymentBenefits]](getBenefits(nino,taxYear,customerEmployment.employmentId,view))
        } yield {
          customerEmployment.toEmploymentSource(employmentData, expenses, benefits)
        }).value
    })
  }

  def getEmploymentData(nino: String, taxYear: Int, employmentId: String, view: String)
                       (implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] ={
    getEmploymentDataConnector.getEmploymentData(nino,taxYear,employmentId,view)
  }

  private def getEmploymentList(nino: String, taxYear: Int)
                 (implicit hc: HeaderCarrier): Future[GetEmploymentListResponse] ={
    getEmploymentListConnector.getEmploymentList(nino,taxYear,None)
  }

  private def getBenefits(nino: String, taxYear: Int, employmentId: String, view: String)
                 (implicit hc: HeaderCarrier): Future[GetEmploymentBenefitsResponse] ={
    getEmploymentBenefitsConnector.getEmploymentBenefits(nino,taxYear,employmentId,view)
  }


  private def getExpenses(nino: String, taxYear: Int, view: String)
                 (implicit hc: HeaderCarrier): Future[GetEmploymentExpensesResponse] ={
    getEmploymentExpensesConnector.getEmploymentExpenses(nino,taxYear,view)
  }

}