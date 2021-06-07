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

package utils

import akka.actor.ActorSystem
import akka.stream.SystemMaterializer
import com.codahale.metrics.SharedMetricRegistries
import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.AppConfig
import controllers.predicates.AuthorisedAction
import models.DES._
import models.frontend._
import models.shared.{Benefits, Expenses, Pay}
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, DefaultActionBuilder, Result}
import play.api.test.{FakeRequest, Helpers}
import services.AuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait TestUtils extends AnyWordSpec with Matchers with MockFactory with GuiceOneAppPerSuite with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
  }

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: SystemMaterializer = SystemMaterializer(actorSystem)

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("mtditid" -> "1234567890")
  val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession("MTDITID" -> "1234567890")
  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val mockControllerComponents: ControllerComponents = Helpers.stubControllerComponents()
  implicit val mockExecutionContext: ExecutionContext = ExecutionContext.Implicits.global
  implicit val mockAuthConnector: AuthConnector = mock[AuthConnector]
  implicit val mockAuthService: AuthService = new AuthService(mockAuthConnector)
  val defaultActionBuilder: DefaultActionBuilder = DefaultActionBuilder(mockControllerComponents.parsers.default)
  val authorisedAction = new AuthorisedAction()(mockAuthConnector, defaultActionBuilder, mockControllerComponents)


  def status(awaitable: Future[Result]): Int = await(awaitable).header.status

  def bodyOf(awaitable: Future[Result]): String = {
    val awaited = await(awaitable)
    await(awaited.body.consumeData.map(_.utf8String))
  }

  val individualEnrolments: Enrolments = Enrolments(Set(
    Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
    Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, "1234567890")), "Activated")))

  //noinspection ScalaStyle
  def mockAuth(enrolments: Enrolments = individualEnrolments): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Individual)))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
      .returning(Future.successful(enrolments and ConfidenceLevel.L200))
  }

  val agentEnrolments: Enrolments = Enrolments(Set(
    Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
    Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
  ))

  //noinspection ScalaStyle
  def mockAuthAsAgent(enrolments: Enrolments = agentEnrolments): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Agent)))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments, *, *)
      .returning(Future.successful(enrolments))
  }

  //noinspection ScalaStyle
  def mockAuthReturnException(exception: Exception): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(exception))
  }

  val allEmploymentData =
    AllEmploymentData(
      Seq(
        EmploymentSource(
          employmentId = "00000000-0000-0000-1111-000000000000",
          employerRef = Some("666/66666"),
          employerName = "Business",
          payrollId = Some("1234567890"),
          startDate = Some("2020-01-01"),
          cessationDate = Some("2020-01-01"),
          dateIgnored = Some("2020-01-01T10:00:38Z"),
          submittedOn = None,
          employmentData = Some(EmploymentData(
            "2020-01-04T05:01:01Z",
            employmentSequenceNumber = Some("1002"),
            companyDirector = Some(false),
            closeCompany = Some(true),
            directorshipCeasedDate = Some("2020-02-12"),
            occPen = Some(false),
            disguisedRemuneration = Some(false),
            Pay(
              taxablePayToDate = 34234.15,
              totalTaxToDate = 6782.92,
              tipsAndOtherPayments = Some(67676),
              payFrequency = Some("CALENDAR MONTHLY"),
              paymentDate = Some("2020-04-23"),
              taxWeekNo = Some(32),
              taxMonthNo = Some(2)
            )
          )),
          employmentBenefits = Some(
            EmploymentBenefits(
              "2020-01-04T05:01:01Z",
              benefits = Some(Benefits(
                Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
              ))
            )
          )
        )
      ),
      hmrcExpenses = Some(
        EmploymentExpenses(
          Some("2020-01-04T05:01:01Z"),
          totalExpenses = Some(800),
          expenses = Some(Expenses(
            Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
          ))
        )
      ),
      Seq(
        EmploymentSource(
          employmentId = "00000000-0000-0000-2222-000000000000",
          employerRef = Some("666/66666"),
          employerName = "Business",
          payrollId = Some("1234567890"),
          startDate = Some("2020-01-01"),
          cessationDate = Some("2020-01-01"),
          dateIgnored = None,
          submittedOn = Some("2020-01-01T10:00:38Z"),
          employmentData = Some(
            EmploymentData(
              "2020-01-04T05:01:01Z",
              employmentSequenceNumber = Some("1002"),
              companyDirector = Some(false),
              closeCompany = Some(true),
              directorshipCeasedDate = Some("2020-02-12"),
              occPen = Some(false),
              disguisedRemuneration = Some(false),
              Pay(
                taxablePayToDate = 34234.15,
                totalTaxToDate = 6782.92,
                tipsAndOtherPayments = Some(67676),
                payFrequency = Some("CALENDAR MONTHLY"),
                paymentDate = Some("2020-04-23"),
                taxWeekNo = Some(32),
                taxMonthNo = Some(2)
              )
            )
          ),
          employmentBenefits = Some(
            EmploymentBenefits(
              "2020-01-04T05:01:01Z",
              benefits = Some(Benefits(
                Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
                Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
              ))
            )
          )
        )
      ),
      customerExpenses = Some(
        EmploymentExpenses(
          Some("2020-01-04T05:01:01Z"),
          totalExpenses = Some(800),
          expenses = Some(Expenses(
            Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
          ))
        )
      )
    )

  val hmrcEmploymentModel: HmrcEmployment =
    HmrcEmployment(
      employmentId = "00000000-0000-0000-1111-000000000000",
      employerRef = Some("666/66666"),
      employerName = "Business",
      payrollId = Some("1234567890"),
      startDate = Some("2020-01-01"),
      cessationDate = Some("2020-01-01"),
      dateIgnored = Some("2020-01-01T10:00:38Z")
    )

  val customerEmploymentModel: CustomerEmployment =
    CustomerEmployment(
      employmentId = "00000000-0000-0000-2222-000000000000",
      employerRef = Some("666/66666"),
      employerName = "Business",
      payrollId = Some("1234567890"),
      startDate = Some("2020-01-01"),
      cessationDate = Some("2020-01-01"),
      submittedOn = "2020-01-01T10:00:38Z"
    )

  val getEmploymentListModelExample: DESEmploymentList =
    DESEmploymentList(
      employments = Seq(hmrcEmploymentModel),
      customerDeclaredEmployments = Seq(customerEmploymentModel)
    )

  val getEmploymentListModelExampleWithNoData: DESEmploymentList =
    DESEmploymentList(
      employments = Seq(),
      customerDeclaredEmployments = Seq()
    )

  val customerEmploymentDataModelExample: DESEmploymentData =
    DESEmploymentData(
      submittedOn = "2020-01-04T05:01:01Z",
      source = Some("CUSTOMER"),
      customerAdded = Some("2020-04-04T01:01:01Z"),
      dateIgnored = Some("2020-04-04T01:01:01Z"),
      employment = DESEmploymentDetails(
        employmentSequenceNumber = Some("1002"),
        payrollId = Some("123456789999"),
        companyDirector = Some(false),
        closeCompany = Some(true),
        directorshipCeasedDate = Some("2020-02-12"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        occPen = Some(false),
        disguisedRemuneration = Some(false),
        employer = Employer(
          employerRef = Some("223/AB12399"),
          employerName = "Business 1"
        ),
        pay = Pay(
          taxablePayToDate = 34234.15,
          totalTaxToDate = 6782.92,
          tipsAndOtherPayments = Some(67676),
          payFrequency = Some("CALENDAR MONTHLY"),
          paymentDate = Some("2020-04-23"),
          taxWeekNo = Some(32),
          taxMonthNo = Some(2)
        )
      )
    )

  val hmrcEmploymentDataModelExample: DESEmploymentData =
    DESEmploymentData(
      submittedOn = "2020-01-04T05:01:01Z",
      source = Some("HMRC-HELD"),
      customerAdded = Some("2020-04-04T01:01:01Z"),
      dateIgnored = Some("2020-04-04T01:01:01Z"),
      employment = DESEmploymentDetails(
        employmentSequenceNumber = Some("1002"),
        payrollId = Some("123456789999"),
        companyDirector = Some(false),
        closeCompany = Some(true),
        directorshipCeasedDate = Some("2020-02-12"),
        startDate = Some("2019-04-21"),
        cessationDate = Some("2020-03-11"),
        occPen = Some(false),
        disguisedRemuneration = Some(false),
        employer = Employer(
          employerRef = Some("223/AB12399"),
          employerName = "Business 1"
        ),
        pay = Pay(
          taxablePayToDate = 34234.15,
          totalTaxToDate = 6782.92,
          tipsAndOtherPayments = Some(67676),
          payFrequency = Some("CALENDAR MONTHLY"),
          paymentDate = Some("2020-04-23"),
          taxWeekNo = Some(32),
          taxMonthNo = Some(2)
        )
      )
    )

  val hmrcBenefits: DESEmploymentBenefits = DESEmploymentBenefits(
    submittedOn = "2020-01-04T05:01:01Z",
    source = Some("HMRC-HELD"),
    customerAdded = None,
    dateIgnored = None,
    employment = EmploymentBenefitsData(
      benefitsInKind = Some(Benefits(
        Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
        Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
        Some(100),Some(100),Some(100),Some(100)
      ))
    )
  )

  val customerBenefits: DESEmploymentBenefits = DESEmploymentBenefits(
    submittedOn = "2020-01-04T05:01:01Z",
    source = Some("CUSTOMER"),
    customerAdded = Some("2020-01-04T05:01:01Z"),
    dateIgnored = None,
    employment = EmploymentBenefitsData(
      benefitsInKind = Some(Benefits(
        Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
        Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),
        Some(100),Some(100),Some(100),Some(100)
      ))
    )
  )

  val hmrcExpenses: DESEmploymentExpenses = DESEmploymentExpenses(
    submittedOn = Some("2020-01-04T05:01:01Z"),
    source = Some("HMRC-HELD"),
    dateIgnored = None,
    totalExpenses = Some(800),
    expenses = Some(Expenses(
      Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
    ))
  )

  val customerExpenses: DESEmploymentExpenses = DESEmploymentExpenses(
    submittedOn = Some("2020-01-04T05:01:01Z"),
    source = Some("CUSTOMER"),
    dateIgnored = None,
    totalExpenses = Some(800),
    expenses = Some(Expenses(
      Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100),Some(100)
    ))
  )

  val getEmploymentDataModelOnlyRequiredExample: DESEmploymentData =
    DESEmploymentData(
      submittedOn = "2020-01-04T05:01:01Z",
      source = None,
      customerAdded = None,
      dateIgnored = None,
      employment = DESEmploymentDetails(
        employmentSequenceNumber = None,
        payrollId = None,
        companyDirector = None,
        closeCompany = None,
        directorshipCeasedDate = None,
        startDate = None,
        cessationDate = None,
        occPen = None,
        disguisedRemuneration = None,
        employer = Employer(
          employerRef = None,
          employerName = "maggie"
        ),
        pay = Pay(
          taxablePayToDate = 34234.15,
          totalTaxToDate = 6782.92,
          tipsAndOtherPayments = None,
          payFrequency = None,
          paymentDate = None,
          taxWeekNo = None,
          taxMonthNo = None,
        )
      )
    )
}

