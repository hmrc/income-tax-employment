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

import com.codahale.metrics.SharedMetricRegistries
import connectors.errors.{ApiError, SingleErrorBody}
import connectors.parsers.GetEmploymentBenefitsHttpParser.GetEmploymentBenefitsResponse
import connectors.parsers.GetEmploymentDataHttpParser.GetEmploymentDataResponse
import connectors.parsers.GetEmploymentExpensesHttpParser.GetEmploymentExpensesResponse
import connectors.parsers.GetEmploymentListHttpParser.GetEmploymentListResponse
import connectors.parsers.OtherEmploymentIncomeHttpParser.OtherEmploymentIncomeResponse
import connectors.{GetEmploymentBenefitsConnector, GetEmploymentDataConnector, GetEmploymentExpensesConnector, GetEmploymentListConnector, OtherEmploymentIncomeConnector}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class EmploymentOrchestrationServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val mtditid = "1234567890"

  val listConnector: GetEmploymentListConnector = mock[GetEmploymentListConnector]
  val dataConnector: GetEmploymentDataConnector = mock[GetEmploymentDataConnector]
  val benefitsConnector: GetEmploymentBenefitsConnector = mock[GetEmploymentBenefitsConnector]
  val expensesConnector: GetEmploymentExpensesConnector = mock[GetEmploymentExpensesConnector]
  val otherEmploymentIncomeConnector: OtherEmploymentIncomeConnector = mock[OtherEmploymentIncomeConnector]
  val service: EmploymentOrchestrationService = new EmploymentOrchestrationService(listConnector, dataConnector, benefitsConnector, expensesConnector, otherEmploymentIncomeConnector)

  "getAllEmploymentData" should {

    "get all the data and form the correct model" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))
      val customerExpectedResult: GetEmploymentDataResponse = Right(Some(customerEmploymentDataModelExample))

      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))
      val customerExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(customerExpenses))

      val otherEmploymentIncomeExpectedResult: OtherEmploymentIncomeResponse = Right(Some(otherEmploymentIncome))

      (listConnector.getEmploymentList(_: String, _: Int, _: Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "CUSTOMER", *)
        .returning(Future.successful(customerExpensesExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      (otherEmploymentIncomeConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(otherEmploymentIncomeExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear, mtditid))

      result mustBe Right(allEmploymentData)
    }

    "forcing an error in the returnError method by sending an empty Seq" in {
      service.returnError(Seq()) mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError()))
    }

    "get all the data and return an error" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))
      val customerExpectedResult: GetEmploymentDataResponse = Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError()))

     // val hmrcBenefitsExpectedResult: GetEmploymentBenefitsResponse = Right(Some(hmrcBenefits))
      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))

      (listConnector.getEmploymentList(_: String, _: Int, _: Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "CUSTOMER", *)
        .returning(Future.successful(hmrcExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerExpectedResult))


      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear, mtditid))

      result mustBe customerExpectedResult
    }
    "get all the data and return an error if expenses call fails" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))
      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError()))

      (listConnector.getEmploymentList(_: String, _: Int, _: Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "CUSTOMER", *)
        .returning(Future.successful(hmrcExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear, mtditid))

      result mustBe hmrcExpensesExpectedResult
    }
    "get all the data and form the correct model when only hmrc data exists but still get customer expenses" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample.copy(customerDeclaredEmployments = None)))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))


      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))
      val customerExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(customerExpenses))

      val otherEmploymentIncomeExpectedResult: OtherEmploymentIncomeResponse = Right(Some(otherEmploymentIncome))

      (listConnector.getEmploymentList(_: String, _: Int, _: Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "CUSTOMER", *)
        .returning(Future.successful(hmrcExpectedResult))


      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "CUSTOMER", *)
        .returning(Future.successful(customerExpensesExpectedResult))

      (otherEmploymentIncomeConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(otherEmploymentIncomeExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear, mtditid))

      result mustBe Right(allEmploymentData.copy(customerEmploymentData = Seq()))
    }
    "get all the data and form the correct model when only customer data exists but still get hmrc expenses" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample.copy(employments = Some(Seq()))))
      val taxYear = 2022
      val nino = "AA123456A"

      val customerExpectedResult: GetEmploymentDataResponse = Right(Some(customerEmploymentDataModelExample))


      val customerExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(customerExpenses))

      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))

      val otherEmploymentIncomeExpectedResult: OtherEmploymentIncomeResponse = Right(Some(otherEmploymentIncome))

      (listConnector.getEmploymentList(_: String, _: Int, _: Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "CUSTOMER", *)
        .returning(Future.successful(customerExpensesExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      (otherEmploymentIncomeConnector.getOtherEmploymentIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(otherEmploymentIncomeExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear, mtditid))

      result mustBe Right(allEmploymentData.copy(hmrcEmploymentData = Seq()))
    }
  }
}
