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

import config.AppConfig
import models.frontend.AllEmploymentData
import models.tasklist._
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListService @Inject()(appConfig: AppConfig,
                                      service: EmploymentOrchestrationService) extends Logging {

  def get(taxYear: Int, nino: String, mtditid: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TaskListSection] = {

    service.getAllEmploymentData(nino, taxYear, mtditid).map {
      case Left(_) => None
      case Right(employments) => getTask(employments, taxYear)
    }.map { optionalTask =>
      TaskListSection(SectionTitle.EmploymentTitle, optionalTask)
    }
  }

  private def getTask(employments: AllEmploymentData, taxYear: Int): Option[Seq[TaskListSectionItem]] = {

    val employmentUrl: String = s"${appConfig.employmentFEBaseUrl}/update-and-submit-income-tax-return/employment-income/$taxYear/employment-summary"

    def employmentTask(taskStatus: TaskStatus): Option[Seq[TaskListSectionItem]] =
      Some(Seq(TaskListSectionItem(TaskTitle.PayeEmployment, taskStatus, Some(employmentUrl))))

    val hmrcSubmittedOn =
      employments.hmrcEmploymentData.map { employment =>
        val submittedOn = employment.hmrcEmploymentFinancialData.flatMap(_.employmentData.map(_.submittedOn)).getOrElse("")
        getSubmittedOnEpoch(submittedOn)
      }

    val customerSubmittedOn =
      employments.hmrcEmploymentData.map { employment =>
      val submittedOn = employment.customerEmploymentFinancialData.flatMap(_.employmentData.map(_.submittedOn)).getOrElse("")
      getSubmittedOnEpoch(submittedOn)
    }

    val hasCustomerData: Boolean = employments.customerEmploymentData.exists(_.employmentData.nonEmpty)

    val hasHmrcLatest = for {
      hmrc <- hmrcSubmittedOn
      customer <- customerSubmittedOn
    } yield {
      hmrc >= customer
    }

    (hasHmrcLatest.contains(true), hasCustomerData) match {
      case (true, _) => employmentTask(TaskStatus.CheckNow)
      case (false, true) => employmentTask(TaskStatus.Completed)
      case (_, _) => None
    }
  }

  private def getSubmittedOnEpoch(timeAsString: String): Long =
    Instant.parse(timeAsString).getEpochSecond
}
