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

import connectors.OtherEmploymentIncomeConnector
import connectors.parsers.OtherEmploymentIncomeHttpParser.OtherEmploymentIncomeResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherEmploymentIncomeService @Inject()(otherEmploymentConnector: OtherEmploymentIncomeConnector) {


    def getOtherEmploymentIncome(nino: String, taxYear: Int, mtditid: String)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OtherEmploymentIncomeResponse] = {
    otherIncome(nino, taxYear, mtditid).flatMap {
      case Right(otherEmploymentIncome) => Future.successful(Right(otherEmploymentIncome))
      case Left(error)                  => Future.successful(Left(error))
    }
  }


  private def otherIncome(nino: String, taxYear: Int, mtditid: String)
                         (implicit hc: HeaderCarrier): Future[OtherEmploymentIncomeResponse] = {
    otherEmploymentConnector.getOtherEmploymentIncome(nino, taxYear)(hc.withExtraHeaders("mtditid" -> mtditid))
  }

}
