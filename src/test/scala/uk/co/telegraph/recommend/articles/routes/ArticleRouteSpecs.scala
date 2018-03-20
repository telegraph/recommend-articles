package uk.co.telegraph.recommend.articles.routes

import akka.util.Timeout
import org.json4s.jackson.JsonMethods
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FreeSpec, Matchers}
import play.api.http._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.co.telegraph.recommend.articles.flows.RecommendArticleFlow
import uk.co.telegraph.recommend.articles.{TestContext, TestData}

import scala.concurrent.Future.successful
import scala.concurrent.duration._
import scala.language.postfixOps

class ArticleRouteSpecs
  extends FreeSpec
  with Matchers
  with MockFactory
  with ScalaFutures
  with TestData
  with TestContext
{
  implicit val timeout        : Timeout        = Timeout( 1000 second )
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(1, Seconds), interval = Span(100, Millis))
  import testActorSystem.dispatcher

  val mockRecommendedArticleFlow:RecommendArticleFlow = stub[RecommendArticleFlow]
  val endpoint = new ArticleRoute(
    recommendedArticleFlow = mockRecommendedArticleFlow,
    controllerComponents   = stubControllerComponents()
  )

  "Given the 'ArticleRoute' routes, " - {
    val request = FakeRequest(HttpVerbs.POST, "/recommend-articles/ping")
      .withHeaders ("Content-Type" -> "application/json")
      .withBody    (sampleRecommendArticleRequest)

    "should be able to get recommendations" in {
      mockRecommendedArticleFlow.getRecommendationFor _ when sampleRecommendArticle returns successful(sampleRecommendArticleResult)

      val result = endpoint.recommendArticles()(request)
      status         (result)(timeout) shouldBe Status.OK
      contentType    (result)(timeout) shouldBe Some("application/json")

      val payload = contentAsString(result)(timeout)
      JsonMethods.parse(payload) shouldBe fromPayloadAsJValue("/response-recommend-article-payload.json")
    }
  }
}
