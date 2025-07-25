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

package services

import connectors.errors.{ApiError, SingleErrorBody}
import connectors.{OtherEmploymentIncomeConnector, OtherEmploymentIncomeIFConnector}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import support.builders.api.OtherEmploymentIncomeBuilder.anOtherEmploymentIncome
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class OtherEmploymentIncomeServiceSpec extends AnyWordSpec  with MockFactory {

  private val nino: String = "AA123456A"
  private val mtdItID: String = "123123123"
  private val taxYear: Int = 2022
  private val taxYear24: Int = 2024

  private val mockOtherEmploymentIncomeConnector = mock[OtherEmploymentIncomeConnector]
  private val mockOtherEmploymentIncomeIFConnector = mock[OtherEmploymentIncomeIFConnector]
  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()
  private val underTest = new OtherEmploymentIncomeService(mockOtherEmploymentIncomeConnector, mockOtherEmploymentIncomeIFConnector)

  "getOtherEmploymentIncome from DES" should {

    " gets no other employment income so returns blank" in {
      (mockOtherEmploymentIncomeConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(Right(None)))

      val result = underTest.getOtherEmploymentIncome(nino, taxYear, mtdItID)
      await(result) mustBe Right(None)
    }

    "gets other employment income and returns as-is with no modifications" in {

      (mockOtherEmploymentIncomeConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(Right(Some(anOtherEmploymentIncome))))

      val result = underTest.getOtherEmploymentIncome(nino, taxYear, mtdItID)
      await(result) mustBe Right(Some(anOtherEmploymentIncome))
    }

    "receives Service Unavailable error from Connector if DES is not accessible" in {
      val serviceUnavailableErrorModel: SingleErrorBody = SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable")
      val desApiError = ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel)

      (mockOtherEmploymentIncomeConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(Left(desApiError)))

      val result = underTest.getOtherEmploymentIncome(nino, taxYear, mtdItID)
      await(result) mustBe Left(desApiError)
    }
  }
  "getOtherEmploymentIncome from IF" should {

    " gets no other employment income so returns blank" in {
      (mockOtherEmploymentIncomeIFConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(Right(None)))

      val result = underTest.getOtherEmploymentIncome(nino, taxYear24, mtdItID)
      await(result) mustBe Right(None)
    }

    "gets other employment income and returns as-is with no modifications" in {

      (mockOtherEmploymentIncomeIFConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(Right(Some(anOtherEmploymentIncome))))

      val result = underTest.getOtherEmploymentIncome(nino, taxYear24, mtdItID)
      await(result) mustBe Right(Some(anOtherEmploymentIncome))
    }

    "receives Service Unavailable error from Connector if DES is not accessible" in {
      val serviceUnavailableErrorModel: SingleErrorBody = SingleErrorBody("SERVICE_UNAVAILABLE", "Service is unavailable")
      val desApiError = ApiError(SERVICE_UNAVAILABLE, serviceUnavailableErrorModel)

      (mockOtherEmploymentIncomeIFConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(*, *, *)
        .returning(Future.successful(Left(desApiError)))

      val result = underTest.getOtherEmploymentIncome(nino, taxYear24, mtdItID)
      await(result) mustBe Left(desApiError)
    }
  }
}
