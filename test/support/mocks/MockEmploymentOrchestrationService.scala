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

package support.mocks

import connectors.errors.ApiError
import models.frontend.AllEmploymentData
import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import services.EmploymentOrchestrationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockEmploymentOrchestrationService extends MockFactory {
  protected val mockEmploymentOrchestrationService: EmploymentOrchestrationService = mock[EmploymentOrchestrationService]
  private type MockType = CallHandler5[String, Int, String, HeaderCarrier, ExecutionContext, Future[Either[ApiError, AllEmploymentData]]]

  def mockGetAllEmploymentData(nino: String,
                               taxYear: Int,
                               mtdItId: String,
                               result: Either[ApiError, AllEmploymentData]): MockType =
    (mockEmploymentOrchestrationService.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(nino, taxYear, mtdItId, *, *)
      .returning(Future.successful(result))

  def mockGetAllEmploymentDataException(nino: String,
                                        taxYear: Int,
                                        mtdItId: String,
                                        result: Throwable): MockType =
    (mockEmploymentOrchestrationService.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(nino, taxYear, mtdItId, *, *)
      .returning(Future.failed(result))

}