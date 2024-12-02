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

import config.{AppConfig, MockAppConfig}
import connectors.errors.{ApiError, SingleErrorBody}
import fixtures.CommonTaskListServiceFixture
import models.frontend.AllEmploymentData
import models.mongo.JourneyAnswers
import models.taskList.TaskStatus.{CheckNow, Completed, InProgress, NotStarted}
import models.taskList._
import play.api.libs.json.{JsObject, JsString, Json}
import support.UnitTest
import support.mocks.{MockEmploymentOrchestrationService, MockJourneyAnswersRepository}
import support.providers.AppConfigStubProvider
import support.utils.TaxYearUtils
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext

class CommonTaskListServiceSpec extends UnitTest
  with AppConfigStubProvider
  with MockJourneyAnswersRepository
  with MockEmploymentOrchestrationService {

  trait Test extends CommonTaskListServiceFixture {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockAppConfig: AppConfig = new MockAppConfig

    val service: CommonTaskListService = new CommonTaskListService(
      appConfig = mockAppConfig,
      service = mockEmploymentOrchestrationService,
      repository = mockJourneyAnswersRepo
    )

    val nino: String = "12345678"
    val mtditid: String = "1234567890"
    val taxYear: Int = TaxYearUtils.taxYear

    val cyaPageUrl: String = s"http://localhost:9317/update-and-submit-income-tax-return/" +
      s"employment-income/$taxYear/employment-summary"

    def toTaskList(status: TaskStatus): TaskListSection = TaskListSection(
      sectionTitle = SectionTitle.EmploymentTitle,
      taskItems = Some(Seq(
        TaskListSectionItem(TaskTitle.PayeEmployment, status, Some(cyaPageUrl))
      ))
    )

    val emptyTaskListResult: TaskListSection = TaskListSection(SectionTitle.EmploymentTitle, None)

    def journeyAnswers(status: String): JourneyAnswers = JourneyAnswers(
      mtdItId = mtditid,
      taxYear = taxYear,
      journey = "employment-summary",
      data = Json.obj("status" -> JsString(status)),
      lastUpdated = Instant.MIN
    )
  }

  "CommonTaskListService.get" when {
    "errors occur" should {
      "return an empty task list when call to retrieve employment summary returns an API error" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Left(ApiError(404, SingleErrorBody("DummyCode", "DummyReason"))))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe emptyTaskListResult
      }

      "throw an exception when call to retrieve employment summary fails" in new Test {
        mockGetAllEmploymentDataException(nino, taxYear, mtditid, new RuntimeException("Dummy error"))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        assertThrows[RuntimeException](result)
      }

      "throw an exception when call to retrieve Journey Answers fails" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(AllEmploymentData(Nil, None, Nil, None, None)))
        mockGetJourneyAnswersException(mtditid, taxYear, "employment-summary", new RuntimeException("Dummy"))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        assertThrows[RuntimeException](result)
      }
    }

    "Employments response contains only HMRC data" should {
      "return expected task list with 'CheckNow' status" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(hmrcOnlyEmploymentData))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe toTaskList(CheckNow)
      }
    }

    "HMRC data in employments response is newer than customer data" should {
      "return expected task list with 'CheckNow' status" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(hmrcLatestResult))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe toTaskList(CheckNow)
      }
    }

    "HMRC data is omitted, or is not latest and Journey Answers are defined" should {
      "return expected task list with status from Journey Answers data if it can be parsed" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(customerAddedOnlyResult))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", Some(journeyAnswers("completed")))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe toTaskList(Completed)
      }

      "throw an exception if an error occurs while parsing Journey Answers status" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(customerLatestResult))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", Some(journeyAnswers("").copy(data = JsObject.empty)))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        assertThrows[RuntimeException](result)
      }

      "return expected task list with 'NotStarted' status if Journey Answers status value is unexpected" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(customerAddedOnlyResult))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", Some(journeyAnswers("dummy")))

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe toTaskList(NotStarted)
      }
    }

    "HMRC data is omitted, or not latest, Journey Answers are not defined, and customer data exists" should {
      "return expected task list with 'InProgress' status if section completed feature switch is enabled" in new Test {
        override val service: CommonTaskListService = new CommonTaskListService(
          appConfig = new MockAppConfig {
            override lazy val sectionCompletedQuestionEnabled: Boolean = true
            override val employmentFEBaseUrl: String = "http://localhost:9317"
          },
          service = mockEmploymentOrchestrationService,
          repository = mockJourneyAnswersRepo
        )

        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(customerAddedOnlyResult))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe toTaskList(InProgress)
      }

      "return expected task list with 'Completed' status if section completed feature switch is disabled" in new Test {
        override val service: CommonTaskListService = new CommonTaskListService(
          appConfig = new MockAppConfig {
            override lazy val sectionCompletedQuestionEnabled: Boolean = false
            override val employmentFEBaseUrl: String = "http://localhost:9317"
          },
          service = mockEmploymentOrchestrationService,
          repository = mockJourneyAnswersRepo
        )

        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(customerLatestResult))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe toTaskList(Completed)
      }
    }

    "no data exists" should {
      "return with an empty task list" in new Test {
        mockGetAllEmploymentData(nino, taxYear, mtditid, Right(AllEmploymentData(Nil, None, Nil, None, None)))
        mockGetJourneyAnswers(mtditid, taxYear, "employment-summary", None)

        def result: TaskListSection = await(service.get(taxYear, nino, mtditid))

        result shouldBe emptyTaskListResult
      }
    }
  }
}
