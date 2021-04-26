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
import connectors.httpParsers.GetEmploymentListHttpParser.GetEmploymentListResponse
import connectors.GetEmploymentListConnector
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class GetEmploymentListServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val connector: GetEmploymentListConnector = mock[GetEmploymentListConnector]
  val service: GetEmploymentListService = new GetEmploymentListService(connector)


  ".getSubmittedDividends" should {

    "return the connector response" in {

      val expectedResult: GetEmploymentListResponse = Right(getEmploymentListModelExample)
      val taxYear = 1234
      val nino = "12345678"

      (connector.getEmploymentList(_: String, _: Int, _:Option[String])(_: HeaderCarrier))
        .expects(nino, taxYear, None, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getEmploymentList(nino, taxYear, None))

      result mustBe expectedResult

    }
  }
}
