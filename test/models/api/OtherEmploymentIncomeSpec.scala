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

package models.api

import play.api.libs.json.{JsValue, Json}
import support.UnitTest
import support.builders.api.LumpSumBuilder.aLumpSum
import support.builders.api.OtherEmploymentIncomeBuilder.anOtherEmploymentIncome

import java.time.Instant

class OtherEmploymentIncomeSpec extends UnitTest {

  private val otherEmploymentIncomeJson: String =
    """
      |{
      |	"submittedOn": "2020-01-04T05:01:01Z",
      |	"shareOption": [
      |		{
      |			"employerName": "Company Ltd",
      |			"employerRef": "321/AB123",
      |			"schemePlanType": "EMI",
      |			"dateOfOptionGrant": "2019-11-20",
      |			"dateOfEvent": "2019-10-20",
      |			"optionNotExercisedButConsiderationReceived": true,
      |			"amountOfConsiderationReceived": 23445.78,
      |			"noOfSharesAcquired": 1,
      |			"classOfSharesAcquired": "FIRST",
      |			"exercisePrice": 456.56,
      |			"amountPaidForOption": 3555.45,
      |			"marketValueOfSharesOnExcise": 3323.45,
      |			"profitOnOptionExercised": 4532.45,
      |			"employersNicPaid": 234.78,
      |			"taxableAmount": 35345.56
      |		}
      |	],
      |	"sharesAwardedOrReceived": [
      |		{
      |			"employerName": "ABC Ltd",
      |			"employerRef": "321/AB156",
      |			"schemePlanType": "SIP",
      |			"dateSharesCeasedToBeSubjectToPlan": "2019-10-20",
      |			"noOfShareSecuritiesAwarded": 2,
      |			"classOfShareAwarded": "FIRST",
      |			"dateSharesAwarded": "2019-09-20",
      |			"sharesSubjectToRestrictions": true,
      |			"electionEnteredIgnoreRestrictions": true,
      |			"actualMarketValueOfSharesOnAward": 35345.67,
      |			"unrestrictedMarketValueOfSharesOnAward": 5643.34,
      |			"amountPaidForSharesOnAward": 4656.45,
      |			"marketValueAfterRestrictionsLifted": 4654.34,
      |			"taxableAmount": 45646.56
      |		}
      |	],
      |	"disability": {
      |		"customerReference": "Customer Reference",
      |		"amountDeducted": 3455.56
      |	},
      |	"foreignService": {
      |		"customerReference": "Foreign Customer Reference",
      |		"amountDeducted": 4232.45
      |	},
      | "lumpSums": [
      |     {
      |         "employerName": "ABC Ltd",
      |         "employerRef": "321/AB156",
      |         "taxableLumpSumsAndCertainIncome": {
      |             "amount": 100.00,
      |             "taxPaid": 23.00,
      |             "taxTakenOffInEmployment": true
      |         },
      |         "benefitFromEmployerFinancedRetirementScheme": {
      |             "amount": 300.00,
      |             "exemptAmount": 100.00,
      |             "taxPaid": 30.00,
      |             "taxTakenOffInEmployment": true
      |         },
      |         "redundancyCompensationPaymentsOverExemption": {
      |             "amount": 200.00,
      |             "taxPaid":20.00,
      |             "taxTakenOffInEmployment": true
      |         },
      |         "redundancyCompensationPaymentsUnderExemption": {
      |             "amount": 2345.78
      |         }
      |     },
      |     {
      |         "employerName": "ABC2 Ltd",
      |         "employerRef": "321/AB158",
      |         "taxableLumpSumsAndCertainIncome": {
      |             "amount": 100.00,
      |             "taxPaid": 23.00,
      |             "taxTakenOffInEmployment": true
      |         },
      |         "benefitFromEmployerFinancedRetirementScheme": {
      |             "amount": 300.00,
      |             "exemptAmount": 100.00,
      |             "taxPaid": 30.00,
      |             "taxTakenOffInEmployment": true
      |         },
      |         "redundancyCompensationPaymentsOverExemption": {
      |             "amount": 200.00,
      |             "taxPaid": 20.00,
      |             "taxTakenOffInEmployment": true
      |         },
      |         "redundancyCompensationPaymentsUnderExemption": {
      |             "amount": 2345.78
      |         }
      |     }
      | ]
      |}
      |""".stripMargin

  private val lumpSum1: LumpSum = aLumpSum
  private val lumpSum2: LumpSum = aLumpSum.copy(
    employerName = "ABC2 Ltd",
    employerRef = "321/AB158"
  )

  private val otherEmploymentIncome = anOtherEmploymentIncome.copy(
    submittedOn = Some(Instant.parse("2020-01-04T05:01:01Z")),
    lumpSums = Some(Set(lumpSum1, lumpSum2))
  )

  ".format" should {
    "be able to write OtherEmploymentIncome object into Json when" when {
      "empty OtherEmploymentIncome object provided" in {
        val jsValue: JsValue = Json.parse(
          """
            |{
            |}
            |""".stripMargin)

        Json.toJson(OtherEmploymentIncome()) shouldBe jsValue
      }

      "full OtherEmploymentIncome object provided" in {
        Json.toJson(otherEmploymentIncome) shouldBe Json.parse(otherEmploymentIncomeJson)
      }
    }

    "be able to parse Json object into OtherEmploymentIncome object when" when {
      "empty json provided" in {
        val jsValue: JsValue = Json.parse(
          """
            |{
            |}
            |""".stripMargin)

        Json.fromJson[OtherEmploymentIncome](jsValue).get shouldBe OtherEmploymentIncome()
      }

      "full json object provided" in {
        Json.fromJson[OtherEmploymentIncome](Json.parse(otherEmploymentIncomeJson)).get shouldBe otherEmploymentIncome
      }
    }
  }
}
