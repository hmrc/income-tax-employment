/*
 * Copyright 2025 HM Revenue & Customs
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

package support.helpers

import connectors.errors.{ApiError, SingleErrorBody}
import models.CreateUpdateEmploymentRequest
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.EmploymentService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockEmploymentService extends MockFactory {
  self: TestSuite =>

  val employmentService: EmploymentService = mock[EmploymentService]
  def mockCreateEmploymentSuccess(): CallHandler4[String, Int, CreateUpdateEmploymentRequest, HeaderCarrier,
    Future[Either[ApiError, Option[String]]]] = {
    (employmentService.createUpdateEmployment(_: String, _: Int, _: CreateUpdateEmploymentRequest)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Right(Some("employmentId"))))
  }

  def mockAmendEmploymentSuccess(): CallHandler4[String, Int, CreateUpdateEmploymentRequest, HeaderCarrier,
    Future[Either[ApiError, Option[String]]]] = {
    (employmentService.createUpdateEmployment(_: String, _: Int, _: CreateUpdateEmploymentRequest)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(Right(None)))
  }

  def mockCreateOrAmendEmploymentFailure(httpStatus: Int): CallHandler4[String, Int, CreateUpdateEmploymentRequest,
    HeaderCarrier, Future[Either[ApiError, Option[String]]]] = {
    val error = Left(ApiError(httpStatus, SingleErrorBody("DES_CODE", "DES_REASON")))
    (employmentService.createUpdateEmployment(_: String, _: Int, _: CreateUpdateEmploymentRequest)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(error))
  }
}
