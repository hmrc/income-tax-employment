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

package services

import common.EnrolmentKeys.nino
import connectors.errors.{ApiError, SingleErrorBody}
import models.api.EmploymentList
import models.frontend.AllEmploymentData
import models.prePopulation.PrePopulationResponse
import play.api.http.Status.IM_A_TEAPOT
import support.UnitTest
import support.mocks.MockEmploymentOrchestrationService
import support.utils.EmploymentListUtils
import support.utils.EmploymentListUtils._

class PrePopulationServiceSpec extends UnitTest
  with MockEmploymentOrchestrationService {

  trait Test {
    val taxYear: Int = 2024
    val service: PrePopulationService = new PrePopulationService(
      service = mockEmploymentOrchestrationService
    )

    val dummyData: PrePopulationResponse = PrePopulationResponse(
      hasEmployment = false
    )

    def orchestrationServiceResult: Either[ApiError, Option[EmploymentList]]

    def getResult: Either[ApiError, PrePopulationResponse] = {
      mockGetEmploymentList(
        taxYear = taxYear,
        nino = nino ,
        result = orchestrationServiceResult
      )
      await(service.get(taxYear, nino, mtdItId).value)
    }
  }

  "get" when {
    val dummyErrorBody: SingleErrorBody = SingleErrorBody("Some", "Error")
    val defaultErrorBody: SingleErrorBody = SingleErrorBody("defaulted", "error")
    val defaultError: ApiError = ApiError(IM_A_TEAPOT, defaultErrorBody)

    "call to retrieve Employment data fails with a non-404 status code" should {
      "return an error" in new Test {
        val orchestrationServiceResult:Either[ApiError, Option[EmploymentList]] = Left(ApiError(500, dummyErrorBody))
        val result: Either[ApiError, PrePopulationResponse] = getResult
        result shouldBe a[Left[_, _]]
        result.swap.getOrElse(defaultError).body shouldBe dummyErrorBody
      }
    }

    "call to retrieve Employment data succeeds, but the response contains no relevant data" should {
      "return a 'no pre-pop' response" in new Test {
        val orchestrationServiceResult:Either[ApiError, Option[EmploymentList]] =
          Right(Option(employments(None,None)))
        val result: Either[ApiError, PrePopulationResponse] = getResult
        result shouldBe a[Right[_, _]]
        result.getOrElse(dummyData) shouldBe PrePopulationResponse.noPrePop
      }
    }

    "call to retrieve Employment data succeeds, and the response contains relevant data" should {
      "return pre-pop flags as 'true' when customer data exists" in new Test {
        val orchestrationServiceResult:Either[ApiError, Option[EmploymentList]] =
          Right(
            Option(
              employments(
                None,
                Option(
                  Seq(
                    EmploymentListUtils.customerEmploymentModel
                  )
                )
              )
            )
          )
        val result: Either[ApiError, PrePopulationResponse] = getResult

        result shouldBe a[Right[_, _]]
        result.getOrElse(PrePopulationResponse.noPrePop) shouldBe PrePopulationResponse(hasEmployment = true)
      }
    }

      "return pre-pop flags as 'true' when HMRC-Held data exists and not ignored" in new Test {

        val orchestrationServiceResult:Either[ApiError, Option[EmploymentList]] =
          Right(
            Option(
              employments(
                Option(
                  Seq(
                    EmploymentListUtils.hmrcEmploymentModel.copy(dateIgnored = None),
                )),
                None
              )
            )
          )
        val result: Either[ApiError, PrePopulationResponse] = getResult

        result shouldBe a[Right[_, _]]
        result.getOrElse(PrePopulationResponse.noPrePop) shouldBe PrePopulationResponse(hasEmployment = true)
      }

      "return pre-pop flags as 'false' when HMRC-held data exists but ignored" in new Test {
        val orchestrationServiceResult:Either[ApiError, Option[EmploymentList]] =
          Right(
            Option(
              employments(
                Option(
                  Seq(
                    EmploymentListUtils.hmrcEmploymentModel
                  )),
                None
              )
            )
          )
        val result: Either[ApiError, PrePopulationResponse] = getResult
        result shouldBe a[Right[_, _]]
        result.getOrElse(dummyData) shouldBe PrePopulationResponse.noPrePop
      }
      "return pre-pop flags as 'true' when HMRC-held multiple data and at least one not ignored" in new Test {
        val orchestrationServiceResult:Either[ApiError, Option[EmploymentList]] =
          Right(
            Option(
              employments(
                Option(
                  Seq(
                    EmploymentListUtils.hmrcEmploymentModel,
                    EmploymentListUtils.hmrcEmploymentModel.copy(dateIgnored = None)
                  )),
                None
              )
            )
          )
        val result: Either[ApiError, PrePopulationResponse] = getResult
        result shouldBe a[Right[_, _]]
        result.getOrElse(dummyData) shouldBe PrePopulationResponse(hasEmployment = true)
      }

  }
}
