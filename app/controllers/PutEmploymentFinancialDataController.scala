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

package controllers

import connectors.httpParsers.PutEmploymentFinancialDataHttpParser.PutEmploymentFinancialDataResponse
import controllers.predicates.AuthorisedAction
import models.DES.DESEmploymentFinancialData
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.PutEmploymentFinancialDataService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PutEmploymentFinancialDataController @Inject()(putEmploymentFinancialDataService: PutEmploymentFinancialDataService,
                                                     cc: ControllerComponents,
                                                     authorisedAction: AuthorisedAction)
                                                    (implicit ec: ExecutionContext) extends BackendController(cc){

  def createOrUpdateEmploymentFinancialData(nino: String, taxYear:Int, employmentId:String): Action[AnyContent] = authorisedAction.async { implicit user =>
    user.request.body.asJson.map(_.validate[DESEmploymentFinancialData]) match {
      case Some(JsSuccess(model, _)) => responseHandler(putEmploymentFinancialDataService.createOrUpdateFinancialData(nino, taxYear, employmentId, model))
      case _ => Future.successful(BadRequest)
    }
  }

  def responseHandler(serviceResponse: Future[PutEmploymentFinancialDataResponse]): Future[Result] ={
    serviceResponse.map {
      case Right(()) => NoContent
      case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
    }
  }
}
