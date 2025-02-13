/*
 * Copyright 2025 HM Revenue & Customs
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

import cats.data.EitherT
import connectors.errors.ApiError
import models.prePopulation.PrePopulationResponse
import uk.gov.hmrc.http.HeaderCarrier
import utils.PrePopulationLogging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrePopulationService @Inject()(service: EmploymentOrchestrationService) extends PrePopulationLogging {
  val classLoggingContext = "PrePopulationService"

  def get(taxYear: Int, nino: String, mtditid: String)
         (implicit ec: ExecutionContext,hc: HeaderCarrier): EitherT[Future, ApiError, PrePopulationResponse] = {

    val methodLoggingContext: String = "get"
    val userDataLogString: String = s" for NINO: $nino, and tax year: $taxYear"
    val downstreamSource: String = if (taxYear > 2023) "IF" else "DES"
    val getInfoLogger = infoLog(methodLoggingContext = methodLoggingContext, dataLog = userDataLogString)

    getInfoLogger(s"Attempting to retrieve user's employment data from $downstreamSource")


    val result  = EitherT(service.getEmploymentList(nino,taxYear)).map{
      case None =>
        (Seq.empty,Seq.empty)
      case Some(l) =>
        (l.employments.getOrElse(Seq.empty), l.customerDeclaredEmployments.getOrElse(Seq.empty))
    }.map{
      case (_, _ +: _ )  =>  //None Empty Customer data
        getInfoLogger(s"Valid Employment data found within success response from $downstreamSource. Determining correct pre-pop response")
        PrePopulationResponse.hasPrePop
      case (hmrcData, _) if hmrcData.exists(_.dateIgnored.isEmpty)=>
        getInfoLogger(s"Valid Employment data found within success response from $downstreamSource. Determining correct pre-pop response")
        PrePopulationResponse.hasPrePop
      case _ =>
        getInfoLogger(s"No Employment data found in success response from $downstreamSource. Returning 'no pre-pop' response")
        PrePopulationResponse.noPrePop
    }

    result.leftMap {
      err =>
        warnLog(methodLoggingContext, userDataLogString)(
          "Attempt to retrieve user's employment data from IF failed" + s" ${err.toLogString}"
        )
        err
    }
  }
}
