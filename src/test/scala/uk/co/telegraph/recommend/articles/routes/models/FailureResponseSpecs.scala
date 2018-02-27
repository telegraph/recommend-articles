package uk.co.telegraph.recommend.articles.routes.models

import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.recommend.articles.routes.models.FailureResponseSpecs._

class FailureResponseSpecs
  extends FreeSpec
  with Matchers
{
  "Given 'FailureResponse'" - {
    "should be possible to create a 'FailureResponse' with nested exceptions" in {
      FailureResponse(sampleException3) shouldBe expectedFailureWithNestedResponse
    }
    "should be possible to create a 'FailureResponse' without nested exceptions" in {
      FailureResponse(sampleException1) shouldBe expectedFailureWithoutNestedResponse
    }
  }
}

object FailureResponseSpecs {

  val sampleException1: RuntimeException = new RuntimeException("exception 1")
  val sampleException2: RuntimeException = new RuntimeException("exception 2", sampleException1)
  val sampleException3: Exception        = new Exception       ("exception 3", sampleException2)

  val expectedFailureWithNestedResponse = FailureResponse(
    message = "exception 3",
    error   = "Exception",
    causes  = Seq(
      FailureCause(message = "exception 2", error = "RuntimeException"),
      FailureCause(message = "exception 1", error = "RuntimeException")
    )
  )

  val expectedFailureWithoutNestedResponse = FailureResponse(
    message = "exception 1",
    error   = "RuntimeException",
    causes  = Seq.empty
  )
}
