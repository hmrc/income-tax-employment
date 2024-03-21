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

package support.stubs

import config.{AppConfig, AppConfigImpl}
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigStub extends MockFactory {

  def config(): AppConfig = new AppConfigImpl(mock[Configuration], mock[ServicesConfig]) {
    private val wireMockPort = 11111

    override val ifsBaseUrl: String = s"http://localhost:$wireMockPort"
    override val desBaseUrl: String = s"http://localhost:$wireMockPort"

    override val desEnv: String       = "test"
    override val desAuthToken: String = "secret"
    override val ifsEnv: String       = "test"

    override def ifsAuthToken(apiVersion: String): String = desAuthToken + s".$apiVersion"
  }
}
