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
import models.frontend.{AllEmploymentData, EmploymentBenefits, EmploymentData, EmploymentExpenses, EmploymentSource}
import models.shared.{Benefits, Expenses, Pay}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

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

      val finalResult = Right(
        AllEmploymentData(
          Seq(
            EmploymentSource(
              employmentId = "00000000-0000-0000-1111-000000000000",
              employerRef = Some("666/66666"),
              employerName = "Business",
              payrollId = Some("1234567890"),
              startDate = Some("2020-01-01"),
              cessationDate = Some("2020-01-01"),
              dateIgnored = Some("2020-01-01T10:00:38Z"),
              submittedOn = None,
              employmentData = Some(EmploymentData(
                "2020-01-04T05:01:01Z",
                employmentSequenceNumber = Some("1002"),
                companyDirector = Some(false),
                closeCompany = Some(true),
                directorshipCeasedDate = Some("2020-02-12"),
                occPen = Some(false),
                disguisedRemuneration = Some(false),
                Pay(
                  taxablePayToDate = 34234.15,
                  totalTaxToDate = 6782.92,
                  tipsAndOtherPayments = Some(67676),
                  payFrequency = "CALENDAR MONTHLY",
                  paymentDate = "2020-04-23",
                  taxWeekNo = Some(32),
                  taxMonthNo = Some(2)
                )
              )),
              employmentBenefits = Some(
                EmploymentBenefits(
                  "2020-01-04T05:01:01Z",
                  benefits = Some(Benefits(
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
                  ))
                )
              ),
              employmentExpenses = Some(
                EmploymentExpenses(
                  Some("2020-01-04T05:01:01Z"),
                  totalExpenses = Some(800),
                  expenses = Some(Expenses(
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
                  ))
                )
              )
            )
          ),Seq(
            EmploymentSource(
              employmentId = "00000000-0000-0000-2222-000000000000",
              employerRef = Some("666/66666"),
              employerName = "Business",
              payrollId = Some("1234567890"),
              startDate = Some("2020-01-01"),
              cessationDate = Some("2020-01-01"),
              dateIgnored = None,
              submittedOn = Some("2020-01-01T10:00:38Z"),
              employmentData = Some(
                EmploymentData(
                  "2020-01-04T05:01:01Z",
                  employmentSequenceNumber = Some("1002"),
                  companyDirector = Some(false),
                  closeCompany = Some(true),
                  directorshipCeasedDate = Some("2020-02-12"),
                  occPen = Some(false),
                  disguisedRemuneration = Some(false),
                  Pay(
                    taxablePayToDate = 34234.15,
                    totalTaxToDate = 6782.92,
                    tipsAndOtherPayments = Some(67676),
                    payFrequency = "CALENDAR MONTHLY",
                    paymentDate = "2020-04-23",
                    taxWeekNo = Some(32),
                    taxMonthNo = Some(2)
                  )
                )
              ),
              employmentBenefits = Some(
                EmploymentBenefits(
                  "2020-01-04T05:01:01Z",
                  benefits = Some(Benefits(
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
                  ))
                )
              ),
              employmentExpenses = Some(
                EmploymentExpenses(
                  Some("2020-01-04T05:01:01Z"),
                  totalExpenses = Some(800),
                  expenses = Some(Expenses(
                    Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
                  ))
                )
              )
            )
          )
        )
      )

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

      result mustBe finalResult

    }
  }
}
