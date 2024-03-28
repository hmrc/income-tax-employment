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

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {

  // Auth Config
  def authBaseUrl: String

  // DES Config
  def desBaseUrl: String
  def desEnv: String
  def desAuthToken: String

  // IFS Config
  def ifsBaseUrl: String
  def ifsEnv: String
  def ifsAuthToken(apiVersion: String): String

  // Expenses Config
  def expensesBaseUrl: String

  // Submission Config
  def submissionBaseUrl: String

  def auditingEnabled: Boolean
  def graphiteHost: String
}

class AppConfigImpl @Inject() (config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  // Auth Config
  override def authBaseUrl: String = servicesConfig.baseUrl("auth")

  // DES Config
  override def desBaseUrl: String   = servicesConfig.baseUrl("des")
  override def desEnv: String       = config.get[String]("microservice.services.des.environment")
  override def desAuthToken: String = config.get[String]("microservice.services.des.authorisation-token")

  // IFS Config
  override def ifsBaseUrl: String = servicesConfig.baseUrl("integration-framework")
  override def ifsEnv: String     = config.get[String]("microservice.services.integration-framework.environment")
  override def ifsAuthToken(apiVersion: String): String =
    config.get[String](s"microservice.services.integration-framework.authorisation-token.$apiVersion")

  // Expenses Config
  override def expensesBaseUrl: String = servicesConfig.baseUrl("income-tax-expenses")

  // Submission Config
  override def submissionBaseUrl: String = s"${servicesConfig.baseUrl("income-tax-submission")}/income-tax-submission-service"

  override def auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  override def graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")

}
