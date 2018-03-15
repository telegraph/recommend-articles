package uk.co.telegraph.recommend.articles.routes

import akka.util.Timeout
import org.json4s.jackson.JsonMethods
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import play.api.http._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.co.telegraph.recommend.articles.{TestContext, TestData}

import scala.concurrent.duration._
import scala.language.postfixOps

class SwaggerRouteSpecs
  extends FreeSpec
  with Matchers
  with ScalaFutures
  with TestData
  with TestContext
{
  implicit val timeout : Timeout        = Timeout( 1000 second )

  val endpoint = new SwaggerRoute(
    controllerComponents   = stubControllerComponents()
  )

  "Given the 'SwaggerRoute' routes, " - {
    val request = FakeRequest(HttpVerbs.GET, "/swagger")

    "should be possible to get the Swagger doc (Yaml format)" in {
      val result = endpoint.swaggerDoc()(request)

      val payload = contentAsString(result)(timeout)

      contentType    (result)(timeout) shouldBe Some("text/yaml")
      payload shouldBe fromPayload("/swagger.yaml")
    }

    "should fall back to Yaml if the format is invalid" in {
      val result = endpoint.swaggerDoc(format = "xml")(request)

      val payload = contentAsString(result)(timeout)

      contentType    (result)(timeout) shouldBe Some("text/yaml")
      payload shouldBe fromPayload("/swagger.yaml")
    }

    "should be possible to get the Swagger doc (Json format)" in {
      val result = endpoint.swaggerDoc(format = "json")(request)

      val payload = contentAsString(result)(timeout)

      contentType    (result)(timeout) shouldBe Some("application/json")
      JsonMethods.parse(payload) should not be null
    }
  }
}
