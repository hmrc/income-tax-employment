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

import cats.data.EitherT
import config.AppConfig
import connectors.errors.ApiError
import models.frontend.{AllEmploymentData, EmploymentFinancialData}
import models.mongo.JourneyAnswers
import models.taskList.TaskStatus.{CheckNow, Completed, InProgress, NotStarted}
import models.taskList._
import play.api.Logging
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListService @Inject()(appConfig: AppConfig,
                                      service: EmploymentOrchestrationService,
                                      repository: JourneyAnswersRepository) extends Logging {

  private def getEmploymentTask(employments: AllEmploymentData,
                                taxYear: Int,
                                journeyAnswersOpt: Option[JourneyAnswers]): Option[Seq[TaskListSectionItem]] = {

    val baseUrl = s"${appConfig.employmentFEBaseUrl}/update-and-submit-income-tax-return"
    val employmentUrl: String = s"$baseUrl/employment-income/$taxYear/employment-summary"

    def employmentTask(taskStatus: TaskStatus): Option[Seq[TaskListSectionItem]] =
      Some(Seq(TaskListSectionItem(TaskTitle.PayeEmployment, taskStatus, Some(employmentUrl))))

    def toSubmittedOn(financialDataOpt: Option[EmploymentFinancialData]): Instant =
      financialDataOpt.flatMap(
        _.employmentData.map(
          data => Instant.parse(data.submittedOn)
        )
      ).getOrElse(Instant.MIN)

    def isHmrcLatest = employments.hmrcEmploymentData.exists(employment => {
      val hmrcHeldSubmittedOn = toSubmittedOn(employment.hmrcEmploymentFinancialData)
      val customerHeldSubmittedOn = toSubmittedOn(employment.customerEmploymentFinancialData)
      !customerHeldSubmittedOn.isAfter(hmrcHeldSubmittedOn)
    })

    val hasCustomerData: Boolean = employments.customerEmploymentData.exists(_.employmentData.nonEmpty)
    val hasHmrcData: Boolean = employments.hmrcEmploymentData.exists(_.hmrcEmploymentFinancialData.nonEmpty)

    (hasHmrcData, hasCustomerData, journeyAnswersOpt) match {
      case (true, _, _) if isHmrcLatest => employmentTask(CheckNow)
      case (_, _, Some(journeyAnswers)) =>
        val status: TaskStatus = journeyAnswers.data.value("status").validate[TaskStatus].asOpt match {
          case Some(TaskStatus.Completed) => Completed
          case Some(TaskStatus.InProgress) => InProgress
          case _ =>
            logger.info("[CommonTaskListService][getStatus] status stored in an invalid format, setting as 'Not yet started'.")
            NotStarted
        }

        employmentTask(status)
      case (_, true, _) => employmentTask(if(appConfig.sectionCompletedQuestionEnabled) InProgress else Completed)
      case (_, _, _) => None
    }
  }

  def get(taxYear: Int, nino: String, mtdItId: String)
         (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TaskListSection] = {

    def taskListSection(tasksOpt: Option[Seq[TaskListSectionItem]]) =
      TaskListSection(SectionTitle.EmploymentTitle, tasksOpt)

    val result: EitherT[Future, ApiError, Option[Seq[TaskListSectionItem]]] = for {
      dataResult <- EitherT(service.getAllEmploymentData(nino, taxYear, mtdItId))
      jaResult <- EitherT.right(repository.get(mtdItId, taxYear, "employment-summary"))
    } yield getEmploymentTask(dataResult, taxYear, jaResult)

    result
      .leftMap(_ => Option.empty[Seq[TaskListSectionItem]])
      .merge
      .map(taskListSection)
  }
}
