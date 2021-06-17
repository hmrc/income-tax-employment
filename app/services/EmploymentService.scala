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
import connectors.httpParsers.CreateEmploymentHttpParser.CreateEmploymentResponse
import connectors.httpParsers.DeleteEmploymentHttpParser.DeleteEmploymentResponse
import connectors.httpParsers.DeleteEmploymentFinancialDataHttpParser.DeleteEmploymentFinancialDataResponse
import models.shared.AddEmploymentRequestModel
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class EmploymentService @Inject()(createEmploymentConnector: CreateEmploymentConnector,
                                  deleteEmploymentConnector: DeleteEmploymentConnector,
                                  deleteEmploymentFinancialDataConnector: DeleteEmploymentFinancialDataConnector) {

  def createEmployment(nino: String, taxYear: Int, employmentModel: AddEmploymentRequestModel)
                      (implicit hc: HeaderCarrier): Future[CreateEmploymentResponse] = {

    createEmploymentConnector.createEmployment(nino, taxYear, employmentModel)

  }

  def deleteEmployment(nino: String, taxYear: Int, employmentId: String)
                      (implicit hc: HeaderCarrier): Future[DeleteEmploymentResponse] = {

    deleteEmploymentConnector.deleteEmployment(nino, taxYear, employmentId)

  }

  def deleteEmploymentFinancialData(nino: String, taxYear: Int, employmentId: String)
                                   (implicit hc: HeaderCarrier): Future[DeleteEmploymentFinancialDataResponse] = {

    deleteEmploymentFinancialDataConnector.deleteEmploymentFinancial(nino, taxYear, employmentId)
  }
}
