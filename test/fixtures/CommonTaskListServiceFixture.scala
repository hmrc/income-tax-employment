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

package fixtures

import models.api
import models.api.{LumpSum, OtherEmploymentIncome, TaxableLumpSumsAndCertainIncome}
import models.frontend.{AllEmploymentData, EmploymentBenefits, EmploymentData, EmploymentExpenses, EmploymentFinancialData, EmploymentSource, HmrcEmploymentSource}
import models.shared.{Benefits, Deductions, Expenses, Pay, StudentLoans}
import support.builders.api.EmploymentDetailsBuilder

trait CommonTaskListServiceFixture {
  val otherEmploymentIncome: OtherEmploymentIncome = OtherEmploymentIncome(
    lumpSums = Some(Set(
      LumpSum(
        employerName = "Business",
        employerRef = "666/66666",
        taxableLumpSumsAndCertainIncome = Some(TaxableLumpSumsAndCertainIncome(
          amount = 100
        )),
        benefitFromEmployerFinancedRetirementScheme = None,
        redundancyCompensationPaymentsOverExemption = None,
        redundancyCompensationPaymentsUnderExemption = None
      )
    ))
  )

  val allEmploymentData: AllEmploymentData =
    AllEmploymentData(
      Seq(
        HmrcEmploymentSource(
          employmentId = "00000000-0000-0000-1111-000000000000",
          employerRef = Some("666/66666"),
          employerName = "Business",
          payrollId = Some("1234567890"),
          startDate = Some("2020-01-01"),
          cessationDate = Some("2020-01-01"),
          occupationalPension = Some(false),
          dateIgnored = Some("2020-01-01T10:00:38Z"),
          submittedOn = None,
          hmrcEmploymentFinancialData = Some(
            EmploymentFinancialData(
              employmentData = Some(EmploymentData(
                "2020-01-04T05:01:01Z",
                employmentSequenceNumber = Some("1002"),
                companyDirector = Some(false),
                closeCompany = Some(true),
                directorshipCeasedDate = Some("2020-02-12"),
                occPen = Some(false),
                disguisedRemuneration = Some(false),
                offPayrollWorker = Some(false),
                Some(Pay(
                  taxablePayToDate = Some(34234.15),
                  totalTaxToDate = Some(6782.92),
                  payFrequency = Some("CALENDAR MONTHLY"),
                  paymentDate = Some("2020-04-23"),
                  taxWeekNo = Some(32),
                  taxMonthNo = Some(2)
                )),
                Some(Deductions(
                  studentLoans = Some(StudentLoans(
                    uglDeductionAmount = Some(100),
                    pglDeductionAmount = Some(100)
                  ))
                ))
              )),
              employmentBenefits = Some(
                EmploymentBenefits(
                  "2020-01-04T05:01:01Z",
                  benefits = Some(Benefits(
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
                  ))
                )
              )
            )
          ),
          customerEmploymentFinancialData = Some(
            EmploymentFinancialData(
              employmentData = Some(EmploymentData(
                "2020-01-04T05:01:01Z",
                employmentSequenceNumber = Some("1002"),
                companyDirector = Some(false),
                closeCompany = Some(true),
                directorshipCeasedDate = Some("2020-02-12"),
                occPen = Some(false),
                disguisedRemuneration = Some(false),
                offPayrollWorker = Some(false),
                Some(Pay(
                  taxablePayToDate = Some(34234.15),
                  totalTaxToDate = Some(6782.92),
                  payFrequency = Some("CALENDAR MONTHLY"),
                  paymentDate = Some("2020-04-23"),
                  taxWeekNo = Some(32),
                  taxMonthNo = Some(2)
                )),
                Some(Deductions(
                  studentLoans = Some(StudentLoans(
                    uglDeductionAmount = Some(100),
                    pglDeductionAmount = Some(100)
                  ))
                ))
              )),
              employmentBenefits = Some(
                EmploymentBenefits(
                  "2020-01-04T05:01:01Z",
                  benefits = Some(Benefits(
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
                  ))
                )
              )
            )
          )
        )
      ),
      hmrcExpenses = Some(
        EmploymentExpenses(
          Some("2020-01-04T05:01:01Z"),
          Some("2020-01-04T05:01:01Z"),
          totalExpenses = Some(800),
          expenses = Some(Expenses(
            Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
          ))
        )
      ),
      Seq(
        EmploymentSource(
          employmentId = "00000000-0000-0000-2222-000000000000",
          employerRef = Some("666/66666"),
          employerName = "Business",
          payrollId = Some("1234567890"),
          startDate = Some("2020-01-01"),
          cessationDate = Some("2020-01-01"),
          occupationalPension = Some(false),
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
              offPayrollWorker = Some(false),
              Some(Pay(
                taxablePayToDate = Some(34234.15),
                totalTaxToDate = Some(6782.92),
                payFrequency = Some("CALENDAR MONTHLY"),
                paymentDate = Some("2020-04-23"),
                taxWeekNo = Some(32),
                taxMonthNo = Some(2)
              )),
              Some(Deductions(
                studentLoans = Some(StudentLoans(
                  uglDeductionAmount = Some(100),
                  pglDeductionAmount = Some(100)
                ))
              ))
            )
          ),
          employmentBenefits = Some(
            EmploymentBenefits(
              "2020-01-04T05:01:01Z",
              benefits = Some(Benefits(
                Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
              ))
            )
          )
        )
      ),
      customerExpenses = Some(
        EmploymentExpenses(
          Some("2020-01-04T05:01:01Z"),
          Some("2020-01-04T05:01:01Z"),
          totalExpenses = Some(800),
          expenses = Some(Expenses(
            Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
          ))
        )
      ),
      otherEmploymentIncome = Some(
        otherEmploymentIncome
      )
    )

  val hmrcOnlyEmploymentData: AllEmploymentData =
    AllEmploymentData(
      hmrcEmploymentData = Seq(
        HmrcEmploymentSource(
          employmentId = "00000000-0000-0000-1111-000000000000",
          employerRef = Some("666/66666"),
          employerName = "Business",
          payrollId = Some("1234567890"),
          startDate = Some("2020-01-01"),
          cessationDate = Some("2020-01-01"),
          occupationalPension = Some(false),
          dateIgnored = Some("2020-01-01T10:00:38Z"),
          submittedOn = None,
          hmrcEmploymentFinancialData = Some(
            EmploymentFinancialData(
              employmentData = Some(EmploymentData(
                "2020-01-04T05:01:01Z",
                employmentSequenceNumber = Some("1002"),
                companyDirector = Some(false),
                closeCompany = Some(true),
                directorshipCeasedDate = Some("2020-02-12"),
                occPen = Some(false),
                disguisedRemuneration = Some(false),
                offPayrollWorker = Some(false),
                Some(Pay(
                  taxablePayToDate = Some(34234.15),
                  totalTaxToDate = Some(6782.92),
                  payFrequency = Some("CALENDAR MONTHLY"),
                  paymentDate = Some("2020-04-23"),
                  taxWeekNo = Some(32),
                  taxMonthNo = Some(2)
                )),
                Some(Deductions(
                  studentLoans = Some(StudentLoans(
                    uglDeductionAmount = Some(100),
                    pglDeductionAmount = Some(100)
                  ))
                ))
              )),
              employmentBenefits = Some(
                EmploymentBenefits(
                  "2020-01-04T05:01:01Z",
                  benefits = Some(Benefits(
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
                    Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
                  ))
                )
              )
            )
          ),
          customerEmploymentFinancialData = None
        )
      ),
      hmrcExpenses = Some(
        EmploymentExpenses(
          Some("2020-01-04T05:01:01Z"),
          Some("2020-01-04T05:01:01Z"),
          totalExpenses = Some(800),
          expenses = Some(Expenses(
            Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
          ))
        )
      ),
      customerEmploymentData = Nil,
      customerExpenses = None,
      otherEmploymentIncome = Some(
        otherEmploymentIncome
      )
    )

  val hmrcLatestResult: AllEmploymentData = allEmploymentData.copy(
    hmrcEmploymentData = Seq(allEmploymentData.hmrcEmploymentData.head.copy(
      hmrcEmploymentFinancialData = Some(EmploymentFinancialData(
        employmentData = Some(models.frontend.EmploymentData(
          desEmploymentData = models.api.EmploymentData(
            submittedOn = "2023-01-04T05:01:01Z",
            source = None,
            customerAdded = None,
            dateIgnored = None,
            employment = EmploymentDetailsBuilder.anEmploymentDetails
          )
        )),
        employmentBenefits = None
      ))
    ))
  )

  val customerEmploymentModel: api.CustomerEmployment =
    api.CustomerEmployment(
      employmentId = "00000000-0000-0000-2222-000000000000",
      employerRef = Some("666/66666"),
      employerName = "Business",
      payrollId = Some("1234567890"),
      startDate = Some("2020-01-01"),
      cessationDate = Some("2020-01-01"),
      occupationalPension = Some(false),
      submittedOn = "2020-01-01T10:00:38Z"
    )

  val customerAddedOnlyResult: AllEmploymentData = allEmploymentData.copy(
    hmrcEmploymentData = Seq.empty,
    hmrcExpenses = None,
    customerEmploymentData = Seq(
      customerEmploymentModel.toEmploymentSource(
        employmentData = Some(models.api.EmploymentData(
          submittedOn = "2023-01-04T05:01:01Z",
          source = None,
          customerAdded = None,
          dateIgnored = None,
          employment = EmploymentDetailsBuilder.anEmploymentDetails
        ))
      )
    ),
    customerExpenses = None,
    otherEmploymentIncome = None
  )

  val customerLatestResult: AllEmploymentData = allEmploymentData.copy(
    Seq(
      allEmploymentData.hmrcEmploymentData.head.copy(
        customerEmploymentFinancialData = Some(EmploymentFinancialData(
          Some(models.frontend.EmploymentData(
            desEmploymentData = models.api.EmploymentData(
              submittedOn = "2023-01-04T05:01:01Z",
              source = None,
              customerAdded = None,
              dateIgnored = None,
              employment = EmploymentDetailsBuilder.anEmploymentDetails
            )
          )),
          employmentBenefits = None
        ))
      ))
  )
}
