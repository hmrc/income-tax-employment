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

package controllers

import models.taskList.{SectionTitle, TaskListSection}
import org.scalamock.handlers.CallHandler5
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import services.CommonTaskListService
import support.helpers.MockAuthHelper
import support.utils.TaxYearUtils
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CommonTaskListControllerSpec extends AnyWordSpec with MockAuthHelper {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val nino: String = "123456789"
  val taxYear: Int = TaxYearUtils.taxYear
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("mtditid" -> "1234567890")

  val commonTaskListService: CommonTaskListService = mock[CommonTaskListService]

  val controller = new CommonTaskListController(commonTaskListService, authorisedAction, Helpers.stubControllerComponents())

  def mockEmploymentService(): CallHandler5[Int, String, String, ExecutionContext, HeaderCarrier, Future[TaskListSection]] = {
    (commonTaskListService.get(_: Int, _: String, _: String)(_: ExecutionContext, _: HeaderCarrier))
      .expects(*, *, *, *, *)
      .returning(Future.successful(TaskListSection(SectionTitle.EmploymentTitle, None)))
  }

  ".getCommonTaskList" should {

    "return a task list section model" in {

      val result = {
        mockAuth()
        mockEmploymentService()
        controller.getCommonTaskList(taxYear, nino)(fakeRequest)
      }

      status(result) mustBe OK
    }
  }
}
