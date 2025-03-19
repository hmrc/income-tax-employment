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

package connectors.parsers

import connectors.errors.{ApiError, MultiErrorsBody, SingleErrorBody}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtils

class DESParserSpec extends TestUtils{

  object FakeParser extends DESParser {
    override val parserName: String = "TestParser"
    override val isDesAPI: Boolean = true
  }

  object FakeAPIParser extends DESParser {
    override val parserName: String = "TestParser"
    override val isDesAPI: Boolean = false
  }

  def httpResponse(json: JsValue =
                   Json.parse(
                     """{"failures":[
                       |{"code":"SERVICE_UNAVAILABLE","reason":"The service is currently unavailable"},
                       |{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}]}""".stripMargin)): HttpResponse = HttpResponse(
    INTERNAL_SERVER_ERROR,
    json,
    Map("CorrelationId" -> Seq("1234645654645"))
  )

  "FakeParser" should {
    "log the correct message" in {
      val result = FakeParser.logMessage(httpResponse())
      result mustBe
        """[TestParser][read] Received 500 from DES. Body:{
          |  "failures" : [ {
          |    "code" : "SERVICE_UNAVAILABLE",
          |    "reason" : "The service is currently unavailable"
          |  }, {
          |    "code" : "INTERNAL_SERVER_ERROR",
          |    "reason" : "The service is currently facing issues."
          |  } ]
          |} CorrelationId: 1234645654645""".stripMargin
    }
    "return the the correct error" in {
      val result = FakeParser.badSuccessJsonFromDES
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from DES")))
    }
    "handle multiple errors" in {
      val result = FakeParser.handleDESError(httpResponse())
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, MultiErrorsBody(Seq(
        SingleErrorBody("SERVICE_UNAVAILABLE", "The service is currently unavailable"),
        SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")
      ))))
    }
    "handle single errors" in {
      val result = FakeParser.handleDESError(httpResponse(Json.parse(
        """{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}""".stripMargin)))
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")))
    }

    "handle response that is neither a single error or multiple errors" in {
      val result = FakeParser.handleDESError(httpResponse(Json.obj()))
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from DES")))
    }

    "handle response when the response body is not json" in {
      val result = FakeParser.handleDESError(HttpResponse(INTERNAL_SERVER_ERROR, "", Map("CorrelationId" -> Seq("1234645654645"))))
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from DES")))
    }

  }

  "FakeAPIParser" should {
    "log the correct message" in {
      val result = FakeAPIParser.logMessage(httpResponse())
      result mustBe
        """[TestParser][read] Received 500 from API. Body:{
          |  "failures" : [ {
          |    "code" : "SERVICE_UNAVAILABLE",
          |    "reason" : "The service is currently unavailable"
          |  }, {
          |    "code" : "INTERNAL_SERVER_ERROR",
          |    "reason" : "The service is currently facing issues."
          |  } ]
          |} CorrelationId: 1234645654645""".stripMargin
    }
    "return the the correct error" in {
      val result = FakeAPIParser.badSuccessJsonFromDES
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from API")))
    }
    "handle multiple errors" in {
      val result = FakeAPIParser.handleDESError(httpResponse())
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, MultiErrorsBody(Seq(
        SingleErrorBody("SERVICE_UNAVAILABLE", "The service is currently unavailable"),
        SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")
      ))))
    }
    "handle single errors" in {
      val result = FakeAPIParser.handleDESError(httpResponse(Json.parse(
        """{"code":"INTERNAL_SERVER_ERROR","reason":"The service is currently facing issues."}""".stripMargin)))
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")))
    }

    "handle response that is neither a single error or multiple errors" in {
      val result = FakeAPIParser.handleDESError(httpResponse(Json.obj()))
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from API")))
    }

    "handle response when the response body is not json" in {
      val result = FakeAPIParser.handleDESError(HttpResponse(INTERNAL_SERVER_ERROR, "", Map("CorrelationId" -> Seq("1234645654645"))))
      result mustBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error parsing response from API")))
    }

  }
}