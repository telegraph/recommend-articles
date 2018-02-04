package uk.co.telegraph.recommend.articles.routes.error

import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Formats}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.co.telegraph.recommend.articles.TestContext
import uk.co.telegraph.recommend.articles.routes.models.FailureResponse
import uk.co.telegraph.utils.client.HttpClientHelper

class CustomErrorHandlerSpec
  extends FreeSpec
    with ScalaFutures
    with Matchers
    with MockFactory
    with HttpClientHelper
    with OneInstancePerTest
    with TestContext
{
  implicit val formats:Formats = DefaultFormats

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))

  val errorHandler = new CustomErrorHandler()

  "Given the custom error hanlder, " - {

    "onServerError - Should map response as InternalServerError with json body" - {
      "When underlying cause is not present" in {

        val request = FakeRequest(POST, s"/ucm-storage-service/content")
          .withHeaders ("Content-Type" -> "application/json")

        val result = errorHandler.onServerError(request, new RuntimeException("Some exception"))

        status(result) shouldBe INTERNAL_SERVER_ERROR
        JsonMethods.parse(contentAsString(result)).extract[FailureResponse] shouldBe FailureResponse(
          message = "Some exception",
          error   = "RuntimeException",
          causes  = Seq()
        )
      }
    }

    "onClientError" - {
      "Should return response as json body" in {

        val request = FakeRequest(POST, s"/ucm-storage-service/content")
          .withHeaders ("Content-Type" -> "application/json")

        val result = errorHandler.onClientError(request, 400, "Some error")

        status(result) shouldBe BAD_REQUEST

        JsonMethods.parse(contentAsString(result)).extract[FailureResponse] shouldBe FailureResponse(
          message = "Some error",
          error   = "",
          causes  = Seq()
        )
      }
    }

  }
}
