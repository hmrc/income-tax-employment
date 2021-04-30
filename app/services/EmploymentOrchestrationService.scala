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
import models.{CustomerEmployment, DesErrorModel, EmploymentList, HmrcEmployment}

import scala.concurrent.{ExecutionContext, Future}

class EmploymentOrchestrationService @Inject()(getEmploymentListConnector: GetEmploymentListConnector,
                                               getEmploymentDataConnector: GetEmploymentDataConnector,
                                               getEmploymentBenefitsConnector: GetEmploymentBenefitsConnector,
                                               getEmploymentExpensesConnector: GetEmploymentExpensesConnector) {

  def getAllEmploymentData(nino: String, taxYear: Int, view: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, Option[EmploymentList]]] = {

    getEmploymentList(nino, taxYear).flatMap {
      case Right(Some(EmploymentList(hmrc,customer))) =>
      case Right(None) => Future(Right(None))
      case Left(error) => Future.successful(Left(error))
    }
  }

  def getHmrcEmploymentData(hmrcEmploymentData: Seq[HmrcEmployment]): Seq[HmrcEmployment] ={

    hmrcEmploymentData.map{
      hmrcEmployment =>

    }
  }

  def getCustomerEmploymentData(customerEmploymentData: Seq[CustomerEmployment]): Seq[CustomerEmployment] ={
    customerEmploymentData.map{
      customerEmployment =>

    }
  }

  def getEmploymentList(nino: String, taxYear: Int)
                 (implicit hc: HeaderCarrier): Future[GetEmploymentListResponse] ={
    getEmploymentListConnector.getEmploymentList(nino,taxYear,None)
  }

  def getBenefits(nino: String, taxYear: Int, employmentId: String, view: String)
                 (implicit hc: HeaderCarrier): Future[GetEmploymentBenefitsResponse] ={
    getEmploymentBenefitsConnector.getEmploymentBenefits(nino,taxYear,employmentId,view)
  }

  def getEmploymentData(nino: String, taxYear: Int, employmentId: String, view: String)
                       (implicit hc: HeaderCarrier): Future[GetEmploymentDataResponse] ={
    getEmploymentDataConnector.getEmploymentData(nino,taxYear,employmentId,view)
  }

  def getExpenses(nino: String, taxYear: Int, view: String)
                 (implicit hc: HeaderCarrier): Future[GetEmploymentExpensesResponse] ={
    getEmploymentExpensesConnector.getEmploymentExpenses(nino,taxYear,view)
  }

}
