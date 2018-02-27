package uk.co.telegraph.recommend.articles

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.scalatest.{FreeSpec, Matchers}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient
import uk.co.telegraph.recommend.articles.flows.{RecommendArticleFlow, RecommendArticleFlowImpl}
import uk.co.telegraph.utils.client.http.impl.HttpClient

class AppModuleSpec
  extends FreeSpec
  with TestContext
  with Matchers
{

  val injector: Injector = new GuiceApplicationBuilder()
    .bindings(bind[ActorSystem].to(testActorSystem))
    .bindings(bind[Materializer].to(testMaterializer))
    .load( new AppModule() )
    .injector()

  "Given 'AppModule'"- {
    "Should Be able to inject" - {
      "StorageClient" in {
        val instance = injector.instanceOf[StorageClient]
        instance shouldBe a [StorageClient]
        instance shouldBe a [HttpClient]
      }
      "RecommenderClient" in {
        val instance = injector.instanceOf[RecommenderClient]
        instance shouldBe a [RecommenderClient]
        instance shouldBe a [HttpClient]
      }
      "RecommendArticleFlow" in {
        val instance = injector.instanceOf[RecommendArticleFlow]
        instance shouldBe a [RecommendArticleFlowImpl]
      }
    }
  }
}
