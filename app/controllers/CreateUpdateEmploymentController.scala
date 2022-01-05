/*
 * Copyright 2022 HM Revenue & Customs
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
import models.frontend.CreatedEmployment
import models.{CreateUpdateEmploymentRequest, DesErrorModel}
import play.api.Logging
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.EmploymentService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class CreateUpdateEmploymentController @Inject()(service: EmploymentService,
                                                 authorisedAction: AuthorisedAction,
                                                 cc: ControllerComponents)
                                                (implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def createUpdateEmployment(nino: String, taxYear:Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    user.request.body.asJson.map(_.validate[CreateUpdateEmploymentRequest]) match {
      case Some(JsSuccess(model@CreateUpdateEmploymentRequest(_, employment, employmentData, _), _)) if employment.isDefined || employmentData.isDefined =>
        responseHandler(service.createUpdateEmployment(nino, taxYear, model))
      case _ =>
        logger.warn("[CreateUpdateEmploymentController][createUpdateEmployment] Create update employment request is invalid," +
          " neither employment information or employment financial data are supplied")
        Future.successful(BadRequest)
    }
  }

  private def responseHandler(serviceResponse: Future[Either[DesErrorModel, Option[String]]]): Future[Result] ={
    serviceResponse.map {
      case Right(Some(employmentId)) => Created(Json.toJson(CreatedEmployment(employmentId)))
      case Right(_) => NoContent
      case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
    }
  }
}
