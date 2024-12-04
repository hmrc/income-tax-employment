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

class MockAppConfig extends AppConfig {

  override val authBaseUrl: String = "/auth"

  override val auditingEnabled: Boolean = true

  override val graphiteHost: String = "/graphite"

  override val desBaseUrl: String = "/des"

  override val integrationFrameworkBaseUrl: String = "/integrationFramework"

  override val expensesBaseUrl: String = "/expenses"

  override val employmentFEBaseUrl: String = "http://localhost:9317"

  override val environment: String = "dev"

  override val authorisationToken: String = "someToken"

  override val integrationFrameworkEnvironment: String = "dev"

  override def integrationFrameworkAuthorisationToken(api: String): String = "someToken"

  override val encryptionKey: String = "dummyEncryptionKey"

  override val mongoJourneyAnswersTTL: Int = 1000

  override val replaceJourneyAnswersIndexes: Boolean = true

  override lazy val sectionCompletedQuestionEnabled: Boolean = false

  override val emaSupportingAgentsEnabled: Boolean = false
}
