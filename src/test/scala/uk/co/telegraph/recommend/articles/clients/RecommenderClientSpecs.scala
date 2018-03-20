package uk.co.telegraph.recommend.articles.clients

import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient.GetRecommendationException
import uk.co.telegraph.recommend.articles.clients.recommender.model.{RecommenderRequest, RecommenderResponse}
import uk.co.telegraph.recommend.articles.clients.recommender.{HttpRecommenderClient, RecommenderClient}
import uk.co.telegraph.recommend.articles.{TestContext, TestDataRecommenderClient}
import uk.co.telegraph.utils.client.HttpClientHelper
import uk.co.telegraph.utils.client.http.impl.HttpClient

class RecommenderClientSpecs
  extends FreeSpec
  with TestContext
  with MockFactory
  with Matchers
  with HttpClientHelper
  with TestDataRecommenderClient
{

  val client:RecommenderClient = new HttpClientMock(RecommenderClient.defaultConfigPath) with HttpRecommenderClient

  "Given a 'RecommenderClient'" - {
    "it should be an Http Client" in {
      RecommenderClient() shouldBe a[HttpClient]
    }

    "when invoked 'getRecommendationFor'" - {
      "it should return recommendations" in {
        HttpConnectorMock.doRequest _ when withRequestMatching(sampleGetRecommendationsForRequest) returns sampleRecommendationResponse once()

        val (pub, sub) = TestSource.probe[RecommenderRequest]
          .via(client.getRecommendationFor)
          .toMat(TestSink.probe[RecommenderResponse])(Keep.both)
          .run()

        sub.request(1)
        pub
          .sendNext(sampleRecommenderRequest)
          .sendComplete()
        sub
          .expectNext(sampleRecommenderResponse)
          .expectComplete()
      }

      "it should skip failed items" in {
        HttpConnectorMock.doRequest _ when sampleGetRecommendationsForRequest returns sampleInvalidResponse once()

        val (pub, sub) = TestSource.probe[RecommenderRequest]
          .via(client.getRecommendationFor)
          .toMat(TestSink.probe[RecommenderResponse])(Keep.both)
          .run()

        sub.request(1)
        pub.sendNext(sampleRecommenderRequest)
        sub.expectError() shouldBe a [GetRecommendationException]
        pub.expectCancellation()
      }
    }
  }

}
