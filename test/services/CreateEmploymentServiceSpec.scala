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

import connectors.CreateEmploymentConnector
import models.shared.{AddEmploymentRequestModel, AddEmploymentResponseModel}
import models.{DesErrorBodyModel, DesErrorModel}
import org.joda.time.DateTime.now
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class CreateEmploymentServiceSpec extends TestUtils {

  val mockConnector = mock[CreateEmploymentConnector]
  val createEmploymentService = new CreateEmploymentService(mockConnector)

  "createEmployment" should {

    val nino = "entity_id"
    val taxYear = 2022
    val addEmploymentRequestModel = AddEmploymentRequestModel(Some("employerRef"), "employerName", now().toString, Some(now().toString), Some("payrollId"))
    val addEmploymentResponseModel = AddEmploymentResponseModel("employerId")

    "return Right containing employmentId" when {

      "createEmployment connector call succeeds" in {
        (mockConnector.createEmployment(_: String, _: Int, _:AddEmploymentRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Right(addEmploymentResponseModel)))

        val result = createEmploymentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

        await(result) mustBe Right(addEmploymentResponseModel)
      }
    }

    "return Left containing DesError" when {
      "the createEmployment connector call fails" in {
        val desError = DesErrorModel(500, DesErrorBodyModel("DES_CODE", "DES_REASON"))

        (mockConnector.createEmployment(_: String, _: Int, _:AddEmploymentRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, addEmploymentRequestModel, *)
          .returning(Future.successful(Left(desError)))

        val result = createEmploymentService.createEmployment(nino, taxYear, addEmploymentRequestModel)

        await(result) mustBe Left(desError)
      }
    }
  }

}
