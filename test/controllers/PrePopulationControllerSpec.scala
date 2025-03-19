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

package controllers

import cats.data.EitherT
import common.EnrolmentKeys.nino
import connectors.errors.{ApiError, SingleErrorBody}
import models.prePopulation.PrePopulationResponse
import org.scalamock.handlers.CallHandler5
import play.api.http.{HeaderNames, Status => TestStatus}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.{DefaultAwaitTimeout, ResultExtractors}
import services.PrePopulationService
import support.UnitTest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class PrePopulationControllerSpec  extends UnitTest
  with DefaultAwaitTimeout
  with ResultExtractors
  with HeaderNames
  with TestStatus {

  val mockPrePopService: PrePopulationService = mock[PrePopulationService]

  def mockGetPrePop(taxYear: Int, nino: String, mtditid:String, result: Either[ApiError, PrePopulationResponse]):
  CallHandler5[Int, String, String, ExecutionContext, HeaderCarrier, EitherT[Future, ApiError, PrePopulationResponse]] =
    (mockPrePopService
      .get(_: Int, _: String, _:String)(_: ExecutionContext, _: HeaderCarrier))
      .expects(taxYear, nino, mtditid, *, *)
      .returning(EitherT(Future.successful(result)))

  trait Test {
    val taxYear: Int = 2024

    val controller = new PrePopulationController(
      service = mockPrePopService,
      auth = authorisedAction,
      cc = mockControllerComponents
    )

    mockAuth()
  }

  "get" when {
    "prePopulationService returns an error" should {
      "return an error" in new Test {
        mockGetPrePop(taxYear, nino, mtdItId, Left(ApiError(599, SingleErrorBody("beep", "boop"))))
        val result: Future[Result] = controller.get(nino, taxYear)(fakeRequest)
        status(result) shouldBe 500
      }
    }

    "prePopulationService returns a pre pop response" should {
      "return it" in new Test {
        mockGetPrePop(
          taxYear = taxYear,
          nino = nino,
          mtditid = mtdItId,
          result = Right(
            PrePopulationResponse(
              hasEmployment = true
            )
          )
        )

        val result: Future[Result] = controller.get(nino, taxYear)(fakeRequest)
        status(result) shouldBe 200
        contentAsJson(result) shouldBe
          Json.parse(
            """
              |{
              |   "hasEmployment": true
              |}
          """.stripMargin
          )
      }
    }
  }

}
