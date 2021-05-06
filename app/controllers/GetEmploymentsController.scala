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
import javax.inject.Inject
import models.frontend.AllEmploymentData
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.EmploymentOrchestrationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class GetEmploymentsController @Inject()(service: EmploymentOrchestrationService,
                                         auth: AuthorisedAction,
                                         cc: ControllerComponents)
                                        (implicit ec: ExecutionContext) extends BackendController(cc) {

  def getEmployments(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    service.getAllEmploymentData(nino, taxYear).map {
      case Right(AllEmploymentData(hmrcEmployments, None, customerEmployments, None)) if hmrcEmployments.isEmpty && customerEmployments.isEmpty => NoContent
      case Right(model) => Ok(Json.toJson(model))
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }
}
