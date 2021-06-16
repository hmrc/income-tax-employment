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
import connectors.PutEmploymentFinancialDataConnector
import connectors.httpParsers.PutEmploymentFinancialDataHttpParser.PutEmploymentFinancialDataResponse
import models.DES.DESEmploymentFinancialData
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class PUTEmploymentFinancialDataServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val connector: PutEmploymentFinancialDataConnector = mock[PutEmploymentFinancialDataConnector]
  val service: PutEmploymentFinancialDataService = new PutEmploymentFinancialDataService(connector)
  val taxYear = 2021
  val nino = "AA1234567A"
  val employmentId = "0000-0000-0000"


  ".createOrUpdateFinancialData" should {

    "return the connector response" in {

      val expectedResult: PutEmploymentFinancialDataResponse = Right(())

      (connector.putEmploymentFinancialData(_: String, _: Int, _: String, _:DESEmploymentFinancialData)(_: HeaderCarrier))
        .expects(nino, taxYear, employmentId, minFinancialData, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.createOrUpdateFinancialData(nino, taxYear, employmentId, minFinancialData))

      result mustBe expectedResult

    }
  }
}
