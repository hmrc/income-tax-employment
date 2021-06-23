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

import connectors.httpParsers.UpdateEmploymentDataHttpParser.UpdateEmploymentDataResponse
import controllers.predicates.AuthorisedAction
import models.shared.EmploymentRequestModel
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.EmploymentService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateEmploymentController @Inject()(service: EmploymentService,
                                           authorisedAction: AuthorisedAction,
                                           cc: ControllerComponents)
                                          (implicit ec: ExecutionContext) extends BackendController(cc) {

  def updateEmployment(nino: String, taxYear:Int, employmentId: String): Action[AnyContent] = authorisedAction.async { implicit user =>
    user.request.body.asJson.map(_.validate[EmploymentRequestModel]) match {
      case Some(JsSuccess(model, _)) => responseHandler(service.updateEmployment(nino, taxYear, employmentId,model))
      case _ => Future.successful(BadRequest)
    }
  }

  private def responseHandler(serviceResponse: Future[UpdateEmploymentDataResponse]): Future[Result] ={
    serviceResponse.map {
      case Right(_) => Status(NO_CONTENT)
      case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
    }
  }
}
