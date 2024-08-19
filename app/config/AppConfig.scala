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

package config

import com.google.inject.ImplementedBy
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

@ImplementedBy(classOf[BackendAppConfig])
trait AppConfig {

  val specificTaxYear: Int = 2024
  val authBaseUrl: String
  val auditingEnabled: Boolean
  val graphiteHost: String

  val desBaseUrl: String
  val integrationFrameworkBaseUrl: String
  val expensesBaseUrl: String
  val employmentFEBaseUrl: String

  val environment: String
  val authorisationToken: String
  val integrationFrameworkEnvironment: String

  def integrationFrameworkAuthorisationToken(api: String): String
}


class BackendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  lazy val authBaseUrl: String = servicesConfig.baseUrl("auth")
  lazy val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  lazy val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  lazy val desBaseUrl: String = servicesConfig.baseUrl("des")
  lazy val integrationFrameworkBaseUrl: String = servicesConfig.baseUrl("integration-framework")

  lazy val expensesBaseUrl: String = servicesConfig.baseUrl("income-tax-expenses")
  lazy val employmentFEBaseUrl: String = config.get[String]("microservice.services.income-tax-employment-frontend.url")

  lazy val environment: String = config.get[String]("microservice.services.des.environment")
  lazy val authorisationToken: String = config.get[String]("microservice.services.des.authorisation-token")
  lazy val integrationFrameworkEnvironment: String = config.get[String]("microservice.services.integration-framework.environment")

  def integrationFrameworkAuthorisationToken(apiVersion: String): String =
    config.get[String](s"microservice.services.integration-framework.authorisation-token.$apiVersion")


}
