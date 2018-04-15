package uk.co.telegraph.recommend.articles.routes.error

import java.lang.reflect.InvocationTargetException

import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Formats, MappingException}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.co.telegraph.recommend.articles.TestContext
import uk.co.telegraph.recommend.articles.routes.models.{FailureResponse, FailureResponseStatus}
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

        val request = FakeRequest(POST, s"/service/content")
          .withHeaders ("Content-Type" -> "application/json")

        val result = errorHandler.onServerError(request, new RuntimeException("Some exception"))

        status(result) shouldBe INTERNAL_SERVER_ERROR
        JsonMethods.parse(contentAsString(result)).extract[FailureResponse] shouldBe FailureResponse(
          FailureResponseStatus(
            statusCode   = 500,
            appErrorCode = 1500,
            message      = "Some exception"
          )
        )
      }
    }

    "onServerError - Should map response as BAD_REQUEST with json body" - {
      "When underlying cause is IllegalArgumentException" in {
        val request = FakeRequest(POST, s"/service/content")
          .withHeaders ("Content-Type" -> "application/json")

        val result = errorHandler.onServerError(request, MappingException("invalid mapping", new InvocationTargetException(new IllegalArgumentException("Some exception"))))

        status(result) shouldBe BAD_REQUEST
        JsonMethods.parse(contentAsString(result)).extract[FailureResponse] shouldBe FailureResponse(
          FailureResponseStatus(
            statusCode   = 400,
            appErrorCode = 1000,
            message      = "Some exception"
          )
        )
      }

      "When underlying cause is MappingException" in {
        val request = FakeRequest(POST, s"/service/content")
          .withHeaders ("Content-Type" -> "application/json")

        val result = errorHandler.onServerError(request, new MappingException("Some exception"))

        status(result) shouldBe BAD_REQUEST
        JsonMethods.parse(contentAsString(result)).extract[FailureResponse] shouldBe FailureResponse(
          FailureResponseStatus(
            statusCode   = 400,
            appErrorCode = 1000,
            message      = "Some exception"
          )
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
          FailureResponseStatus(
            statusCode   = 400,
            appErrorCode = 1400,
            message      = "Some error"
          )
        )
      }
    }
  }
}
