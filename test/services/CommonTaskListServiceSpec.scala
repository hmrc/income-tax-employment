/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.errors.{ApiError, SingleErrorBody}
import models.frontend.{AllEmploymentData, EmploymentData, EmploymentFinancialData, HmrcEmploymentSource}
import models.tasklist._
import org.scalamock.handlers.CallHandler5
import play.api.http.Status.NOT_FOUND
import support.builders.api.EmploymentDetailsBuilder
import support.providers.AppConfigStubProvider
import support.utils.TaxYearUtils
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListServiceSpec extends TestUtils with AppConfigStubProvider {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val employmentService: EmploymentOrchestrationService = mock[EmploymentOrchestrationService]

  private val service: CommonTaskListService = new CommonTaskListService(mockAppConfig, employmentService)

  private val nino: String = "12345678"
  private val mtditid: String = "1234567890"
  private val taxYear: Int = TaxYearUtils.taxYear

  val cyaPageUrl: String = s"http://localhost:9317/update-and-submit-income-tax-return/employment-income/$taxYear/employment-summary"

  val fullResult: Right[Nothing, AllEmploymentData] = Right(allEmploymentData)

  val customerLatestResult: Right[Nothing, AllEmploymentData] = Right(
    allEmploymentData.copy(Seq(allEmploymentData.hmrcEmploymentData.head.copy(
      customerEmploymentFinancialData =
        Some(EmploymentFinancialData(Some(models.frontend.EmploymentData(models.api.EmploymentData("2023-01-04T05:01:01Z", None, None, None, EmploymentDetailsBuilder.anEmploymentDetails))), None))
    )))
  )

  val customerAddedOnlyResult: Right[Nothing, AllEmploymentData] =
    Right(allEmploymentData.copy(
      Seq.empty,
      None,
      Seq(customerEmploymentModel.toEmploymentSource(Some(models.api.EmploymentData("2023-01-04T05:01:01Z", None, None, None, EmploymentDetailsBuilder.anEmploymentDetails)))),
      None,
      None
    ))

  val checkNowTaskSection: TaskListSection =
    TaskListSection(
      SectionTitle.EmploymentTitle,
      Some(List(TaskListSectionItem(TaskTitle.PayeEmployment, TaskStatus.CheckNow, Some(cyaPageUrl))))
    )

  val completedTaskSection: TaskListSection =
    TaskListSection(
      SectionTitle.EmploymentTitle,
      Some(List(TaskListSectionItem(TaskTitle.PayeEmployment, TaskStatus.Completed, Some(cyaPageUrl))))
    )

  val emptyTaskSection: TaskListSection = TaskListSection(SectionTitle.EmploymentTitle, None)

  def mockGetAllEmploymentData(response: Either[ApiError, AllEmploymentData]): CallHandler5[String, Int, String, HeaderCarrier, ExecutionContext, Future[Either[ApiError, AllEmploymentData]]] =
    (employmentService.getAllEmploymentData(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *, *)
      .returning(Future.successful(response))

  "CommonTaskListService.get" should {

    "return the employment task list section" in {

      mockGetAllEmploymentData(fullResult)

      val underTest = service.get(taxYear, nino, mtditid)

      await(underTest) mustBe checkNowTaskSection
    }

    "return an empty task list section when API response is NOT_FOUND" in {

      val response = Left(ApiError(NOT_FOUND, SingleErrorBody("NOT_FOUND", "No data was found")))

      mockGetAllEmploymentData(response)

      val underTest = service.get(taxYear, nino, mtditid)

      await(underTest) mustBe emptyTaskSection
    }

    "return an empty task list section with empty data" in {

      val response = Right(AllEmploymentData(Seq.empty, None, Seq.empty, None, None))

      mockGetAllEmploymentData(response)

      val underTest = service.get(taxYear, nino, mtditid)

      await(underTest) mustBe emptyTaskSection
    }

    "return tasks as completed when customer data is more recent than hmrc data" in {

      mockGetAllEmploymentData(customerLatestResult)

      val underTest = service.get(taxYear, nino, mtditid)

      await(underTest) mustBe completedTaskSection
    }

    "return tasks as completed when only customer added data exists" in {

      mockGetAllEmploymentData(customerAddedOnlyResult)

      val underTest = service.get(taxYear, nino, mtditid)

      await(underTest) mustBe completedTaskSection
    }
  }
}
