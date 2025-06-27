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

package utils

import models._
import models.api.{LumpSum, OtherEmploymentIncome, TaxableLumpSumsAndCertainIncome}
import models.frontend._
import models.shared._

object TestData {

  val minFinancialData: api.EmploymentFinancialData =
    api.EmploymentFinancialData(
      api.Employment(
        api.PayModel(
          100.00,
          100.00,
          None
        ),
        None,
        None,
        None
      )
    )

  val maxFinancialData: api.EmploymentFinancialData =
    api.EmploymentFinancialData(
      api.Employment(
        api.PayModel(
          100.00,
          100.00,
          Some(100.00)
        ),
        Some(Deductions(
          studentLoans = Some(StudentLoans(
            uglDeductionAmount = Some(100.00),
            pglDeductionAmount = Some(100.00)
          ))
        )),
        Some(Benefits(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
        )),
        Some(true)
      )
    )

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

  val hmrcEmploymentModel: api.HmrcEmployment =
    api.HmrcEmployment(
      employmentId = "00000000-0000-0000-1111-000000000000",
      employerRef = Some("666/66666"),
      employerName = "Business",
      payrollId = Some("1234567890"),
      startDate = Some("2020-01-01"),
      cessationDate = Some("2020-01-01"),
      occupationalPension = Some(false),
      dateIgnored = Some("2020-01-01T10:00:38Z")
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

  val getEmploymentListModelExample: api.EmploymentList =
    api.EmploymentList(
      employments = Some(Seq(hmrcEmploymentModel)),
      customerDeclaredEmployments = Some(Seq(customerEmploymentModel))
    )

  val getEmploymentListModelExampleWithNoData: api.EmploymentList =
    api.EmploymentList(
      employments = None,
      customerDeclaredEmployments = None
    )

  val customerEmploymentDataModelExample: api.EmploymentData =
    api.EmploymentData(
      submittedOn = "2020-01-04T05:01:01Z",
      source = Some("CUSTOMER"),
      customerAdded = Some("2020-04-04T01:01:01Z"),
      dateIgnored = Some("2020-04-04T01:01:01Z"),
      employment = api.EmploymentDetails(
        employmentSequenceNumber = Some("1002"),
        payrollId = Some("123456789999"),
        companyDirector = Some(false),
        closeCompany = Some(true),
        directorshipCeasedDate = Some("2020-02-12"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        occPen = Some(false),
        disguisedRemuneration = Some(false),
        offPayrollWorker = Some(false),
        employer = api.Employer(
          employerRef = Some("223/AB12399"),
          employerName = "Business 1"
        ),
        pay = Some(Pay(
          taxablePayToDate = Some(34234.15),
          totalTaxToDate = Some(6782.92),
          payFrequency = Some("CALENDAR MONTHLY"),
          paymentDate = Some("2020-04-23"),
          taxWeekNo = Some(32),
          taxMonthNo = Some(2)
        )),
        Some(Deductions(
          studentLoans = Some(StudentLoans(
            uglDeductionAmount = Some(100.00),
            pglDeductionAmount = Some(100.00)
          ))
        )),
        benefitsInKind = Some(Benefits(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100)
        ))
      )
    )

  val hmrcEmploymentDataModelExample: api.EmploymentData =
    api.EmploymentData(
      submittedOn = "2020-01-04T05:01:01Z",
      source = Some("HMRC-HELD"),
      customerAdded = Some("2020-04-04T01:01:01Z"),
      dateIgnored = Some("2020-04-04T01:01:01Z"),
      employment = api.EmploymentDetails(
        employmentSequenceNumber = Some("1002"),
        payrollId = Some("123456789999"),
        companyDirector = Some(false),
        closeCompany = Some(true),
        directorshipCeasedDate = Some("2020-02-12"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        occPen = Some(false),
        disguisedRemuneration = Some(false),
        offPayrollWorker = Some(false),
        employer = api.Employer(
          employerRef = Some("223/AB12399"),
          employerName = "Business 1"
        ),
        pay = Some(Pay(
          taxablePayToDate = Some(34234.15),
          totalTaxToDate = Some(6782.92),
          payFrequency = Some("CALENDAR MONTHLY"),
          paymentDate = Some("2020-04-23"),
          taxWeekNo = Some(32),
          taxMonthNo = Some(2)
        )),
        Some(Deductions(
          studentLoans = Some(StudentLoans(
            uglDeductionAmount = Some(100.00),
            pglDeductionAmount = Some(100.00)
          ))
        )),
        benefitsInKind = Some(Benefits(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100)
        ))
      ),

    )

  val hmrcBenefits: api.DESEmploymentBenefits = api.DESEmploymentBenefits(
    submittedOn = "2020-01-04T05:01:01Z",
    source = Some("HMRC-HELD"),
    customerAdded = None,
    dateIgnored = None,
    employment = api.EmploymentBenefitsData(
      benefitsInKind = Some(Benefits(
        Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
        Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
        Some(100), Some(100), Some(100), Some(100)
      ))
    )
  )

  val customerBenefits: api.DESEmploymentBenefits = api.DESEmploymentBenefits(
    submittedOn = "2020-01-04T05:01:01Z",
    source = Some("CUSTOMER"),
    customerAdded = Some("2020-01-04T05:01:01Z"),
    dateIgnored = None,
    employment = api.EmploymentBenefitsData(
      benefitsInKind = Some(Benefits(
        Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
        Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
        Some(100), Some(100), Some(100), Some(100)
      ))
    )
  )

  val hmrcExpenses: api.EmploymentExpenses = api.EmploymentExpenses(
    submittedOn = Some("2020-01-04T05:01:01Z"),
    source = Some("HMRC-HELD"),
    dateIgnored = Some("2020-01-04T05:01:01Z"),
    totalExpenses = Some(800),
    expenses = Some(Expenses(
      Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
    ))
  )

  val customerExpenses: api.EmploymentExpenses = api.EmploymentExpenses(
    submittedOn = Some("2020-01-04T05:01:01Z"),
    source = Some("CUSTOMER"),
    dateIgnored = Some("2020-01-04T05:01:01Z"),
    totalExpenses = Some(800),
    expenses = Some(Expenses(
      Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100)
    ))
  )

  val getEmploymentDataModelOnlyRequiredExample: api.EmploymentData =
    api.EmploymentData(
      submittedOn = "2020-01-04T05:01:01Z",
      source = None,
      customerAdded = None,
      dateIgnored = None,
      employment = api.EmploymentDetails(
        employmentSequenceNumber = None,
        payrollId = None,
        companyDirector = None,
        closeCompany = None,
        directorshipCeasedDate = None,
        startDate = None,
        cessationDate = None,
        occPen = None,
        disguisedRemuneration = None,
        offPayrollWorker = None,
        employer = api.Employer(
          employerRef = None,
          employerName = "maggie"
        ),
        pay = Some(Pay(
          taxablePayToDate = Some(34234.15),
          totalTaxToDate = Some(6782.92),
          payFrequency = None,
          paymentDate = None,
          taxWeekNo = None,
          taxMonthNo = None,
        )),
        None,
        benefitsInKind = Some(Benefits(
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100), Some(100),
          Some(100), Some(100), Some(100), Some(100)
        ))
      )
    )
}

