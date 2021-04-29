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

import controllers.predicates.AuthorisedAction
import models.DesErrorBodyModel.invalidView
import play.api.Logger.logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.GetEmploymentDataService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.ViewParameterValidation.isValid

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GetEmploymentDataController @Inject()(
                                             service: GetEmploymentDataService,
                                             auth: AuthorisedAction,
                                             cc: ControllerComponents
                                           )(implicit ec: ExecutionContext) extends BackendController(cc) {

  def getEmploymentData(nino: String, taxYear: Int, employmentId: String, view: String): Action[AnyContent] = auth.async { implicit user =>
    if(isValid(view)) {
      service.getEmploymentData(nino, taxYear, employmentId, view).map {
        case Right(model) => Ok(Json.toJson(model))
        case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
      }
    } else {
      logger.error(s"[GetEmploymentBenefitsController][getEmploymentBenefits] Supplied view is invalid. View: $view")
      Future(BadRequest(Json.toJson(invalidView)))
    }
  }
}