package uk.co.telegraph.recommend.articles.routes.models

import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.recommend.articles.routes.models.FailureResponseSpecs._

class FailureResponseSpecs
  extends FreeSpec
  with Matchers
{
  "Given 'FailureResponse'" - {
    "should be possible to create a 'FailureResponse' with nested exceptions" in {
      FailureResponse(sampleException3) shouldBe expectedFailureWithExceptionResponse
    }
    "should be possible to create a 'FailureResponse' without nested exceptions" in {
      FailureResponse(statusCode = 400, appErrorCode = 1000, message = "error") shouldBe expectedFailureResponse
    }
  }
}

object FailureResponseSpecs {

  val sampleException1: RuntimeException = new RuntimeException("exception 1")
  val sampleException2: RuntimeException = new RuntimeException("exception 2", sampleException1)
  val sampleException3: Exception        = new Exception       ("exception 3", sampleException2)

  val expectedFailureWithExceptionResponse = FailureResponse(FailureResponseStatus(
      statusCode = 500,
      appErrorCode = 1500,
      message = "exception 3"
    )
  )

  val expectedFailureResponse = FailureResponse(FailureResponseStatus(
    statusCode = 400,
    appErrorCode = 1000,
    message = "error"
  ))
}
