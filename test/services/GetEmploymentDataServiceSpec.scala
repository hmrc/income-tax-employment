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
import connectors.GetEmploymentDataConnector
import connectors.httpParsers.GetEmploymentDataHttpParser.GetEmploymentDataResponse
import models.{DesErrorBodyModel, DesErrorModel}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils
import play.api.http.Status._

import scala.concurrent.Future

class GetEmploymentDataServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val connector: GetEmploymentDataConnector = mock[GetEmploymentDataConnector]
  val service: GetEmploymentDataService = new GetEmploymentDataService(connector)


  ".getSubmittedDividends" should {

    "return the connector response for customer" in {

      val expectedResult: GetEmploymentDataResponse = Right(getEmploymentDataModelExample)
      val taxYear = 1234
      val nino = "12345678"
      val view = "CUSTOMER"
      val employmentId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934"

      (connector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, employmentId, view, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getEmploymentData(nino, taxYear, employmentId, view))

      result mustBe expectedResult

    }

    "return the connector response for hmrc" in {

      val expectedResult: GetEmploymentDataResponse = Right(getEmploymentDataModelExample)
      val taxYear = 1234
      val nino = "12345678"
      val view = "HMRC_HELD"
      val employmentId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934"

      (connector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, employmentId, view, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getEmploymentData(nino, taxYear, employmentId, view))

      result mustBe expectedResult

    }

    "return the connector response for latest" in {

      val expectedResult: GetEmploymentDataResponse = Right(getEmploymentDataModelExample)
      val taxYear = 1234
      val nino = "12345678"
      val view = "LATEST"
      val employmentId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934"

      (connector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
        .expects(nino, taxYear, employmentId, view, *)
        .returning(Future.successful(expectedResult))

      val result = await(service.getEmploymentData(nino, taxYear, employmentId, view))

      result mustBe expectedResult

    }

    val errors: Seq[DesErrorModel] = Seq(
      DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_TAXABLE_ENTITY_ID", "Submission has not passed validation. Invalid parameter taxableEntityId.")),
      DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear.")),
      DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_EMPLOYMENT_ID", "Submission has not passed validation. Invalid parameter employmentId.")),
      DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_VIEW", "Submission has not passed validation. Invalid query parameter view.")),
      DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header parameter CorrelationId.")),
      DesErrorModel(NOT_FOUND, DesErrorBodyModel("NO_DATA_FOUND", "The remote endpoint has indicated that no data can be found.")),
      DesErrorModel(UNPROCESSABLE_ENTITY, DesErrorBodyModel("INVALID_DATE_RANGE", "The remote endpoint has indicated that tax year requested exceeds CY-4.")),
      DesErrorModel(UNPROCESSABLE_ENTITY, DesErrorBodyModel("TAX_YEAR_NOT_SUPPORTED",
        "The remote endpoint has indicated that requested tax year is not supported.")),
      DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")),
      DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")),
    )

    errors.foreach(
      error =>
        s"return the connector response for error case: ${error.toJson}" in {

          val expectedResult: GetEmploymentDataResponse = Left(error)
          val taxYear = 2022
          val nino = "AA123456A"
          val view = "CUSTOMER"
          val id = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934"

          (connector.getEmploymentData(_: String, _: Int, _: String, _: String)(_: HeaderCarrier))
            .expects(nino, taxYear, id, view, *)
            .returning(Future.successful(expectedResult))

          val result = await(service.getEmploymentData(nino, taxYear, id, view))

          result mustBe expectedResult

        }
    )
  }
}
