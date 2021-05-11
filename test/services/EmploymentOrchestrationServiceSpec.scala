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

import com.codahale.metrics.SharedMetricRegistries
import connectors.httpParsers.GetEmploymentBenefitsHttpParser.GetEmploymentBenefitsResponse
import connectors.httpParsers.GetEmploymentDataHttpParser.GetEmploymentDataResponse
import connectors.httpParsers.GetEmploymentExpensesHttpParser.GetEmploymentExpensesResponse
import connectors.httpParsers.GetEmploymentListHttpParser.GetEmploymentListResponse
import connectors.{GetEmploymentBenefitsConnector, GetEmploymentDataConnector, GetEmploymentExpensesConnector, GetEmploymentListConnector}
import models.{DesErrorBodyModel, DesErrorModel}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils
import play.api.http.Status.INTERNAL_SERVER_ERROR

import scala.concurrent.Future

class EmploymentOrchestrationServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val listConnector: GetEmploymentListConnector = mock[GetEmploymentListConnector]
  val dataConnector: GetEmploymentDataConnector = mock[GetEmploymentDataConnector]
  val benefitsConnector: GetEmploymentBenefitsConnector = mock[GetEmploymentBenefitsConnector]
  val expensesConnector: GetEmploymentExpensesConnector = mock[GetEmploymentExpensesConnector]
  val service: EmploymentOrchestrationService = new EmploymentOrchestrationService(listConnector,dataConnector,benefitsConnector,expensesConnector)

  "getAllEmploymentData" should {

    "get all the data and form the correct model" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))
      val customerExpectedResult: GetEmploymentDataResponse = Right(Some(customerEmploymentDataModelExample))

      val hmrcBenefitsExpectedResult: GetEmploymentBenefitsResponse = Right(Some(hmrcBenefits))
      val customerBenefitsExpectedResult: GetEmploymentBenefitsResponse = Right(Some(customerBenefits))

      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))
      val customerExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(customerExpenses))

      (listConnector.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerExpectedResult))

      (benefitsConnector.getEmploymentBenefits(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcBenefitsExpectedResult))

      (benefitsConnector.getEmploymentBenefits(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerBenefitsExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "CUSTOMER", *)
        .returning(Future.successful(customerExpensesExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear))

      result mustBe Right(allEmploymentData)
    }

    "forcing an error in the returnError method by sending an empty Seq" in {
      service.returnError(Seq()) mustBe Left(DesErrorModel(INTERNAL_SERVER_ERROR,DesErrorBodyModel.parsingError()))
    }

    "get all the data and return an error" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))
      val customerExpectedResult: GetEmploymentDataResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR,DesErrorBodyModel.parsingError()))

      val hmrcBenefitsExpectedResult: GetEmploymentBenefitsResponse = Right(Some(hmrcBenefits))
      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))

      (listConnector.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerExpectedResult))

      (benefitsConnector.getEmploymentBenefits(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcBenefitsExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear))

      result mustBe customerExpectedResult
    }
    "get all the data and return an error if expenses call fails" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))
      val hmrcBenefitsExpectedResult: GetEmploymentBenefitsResponse = Right(Some(hmrcBenefits))
      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR,DesErrorBodyModel.parsingError()))

      (listConnector.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (benefitsConnector.getEmploymentBenefits(_: String, _: Int, _:String,  _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcBenefitsExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear))

      result mustBe hmrcExpensesExpectedResult
    }
    "get all the data and form the correct model when only hmrc data exists but still get customer expenses" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample.copy(customerDeclaredEmployments = Seq())))
      val taxYear = 2022
      val nino = "AA123456A"

      val hmrcExpectedResult: GetEmploymentDataResponse = Right(Some(hmrcEmploymentDataModelExample))

      val hmrcBenefitsExpectedResult: GetEmploymentBenefitsResponse = Right(Some(hmrcBenefits))

      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))
      val customerExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(customerExpenses))

      (listConnector.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpectedResult))

      (benefitsConnector.getEmploymentBenefits(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-1111-000000000000", "HMRC-HELD", *)
        .returning(Future.successful(hmrcBenefitsExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "CUSTOMER", *)
        .returning(Future.successful(customerExpensesExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear))

      result mustBe Right(allEmploymentData.copy(customerEmploymentData = Seq()))
    }
    "get all the data and form the correct model when only customer data exists but still get hmrc expenses" in {

      val listExpectedResult: GetEmploymentListResponse = Right(Some(getEmploymentListModelExample.copy(employments = Seq())))
      val taxYear = 2022
      val nino = "AA123456A"

      val customerExpectedResult: GetEmploymentDataResponse = Right(Some(customerEmploymentDataModelExample))

      val customerBenefitsExpectedResult: GetEmploymentBenefitsResponse = Right(Some(customerBenefits))

      val customerExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(customerExpenses))

      val hmrcExpensesExpectedResult: GetEmploymentExpensesResponse = Right(Some(hmrcExpenses))

      (listConnector.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(listExpectedResult))

      (dataConnector.getEmploymentData(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerExpectedResult))

      (benefitsConnector.getEmploymentBenefits(_: String, _: Int, _:String, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "00000000-0000-0000-2222-000000000000", "CUSTOMER", *)
        .returning(Future.successful(customerBenefitsExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "CUSTOMER", *)
        .returning(Future.successful(customerExpensesExpectedResult))

      (expensesConnector.getEmploymentExpenses(_: String, _: Int, _:String)(_: HeaderCarrier))
        .expects(nino, taxYear, "HMRC-HELD", *)
        .returning(Future.successful(hmrcExpensesExpectedResult))

      val result = await(service.getAllEmploymentData(nino, taxYear))

      result mustBe Right(allEmploymentData.copy(hmrcEmploymentData = Seq()))
    }
  }
}
