package uk.co.telegraph.recommend.articles.flows

import akka.NotUsed
import akka.stream.scaladsl.Flow
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.recommend.articles.TestContext._
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient
import uk.co.telegraph.recommend.articles.clients.recommender.model.{RecommenderRequest, RecommenderResponse}
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient.ContentId
import uk.co.telegraph.recommend.articles.routes.models.RecommendArticleResult
import uk.co.telegraph.recommend.articles.{TestContext, TestData}
import uk.co.telegraph.ucm.domain.UnifiedContentModel
import uk.co.telegraph.utils.client.models.ClientDetails

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class RecommendArticleFlowSpecs
  extends FreeSpec
  with TestContext
  with TestData
  with ScalaFutures
  with MockFactory
  with Matchers
{

  val mockStorageClientFunc: Set[ContentId] => Seq[UnifiedContentModel] = stub[Set[ContentId] => Seq[UnifiedContentModel] ]

  val mockStorageClient    : StorageClient     = new StorageClient {
    val getByIds: Flow[Set[ContentId], Seq[UnifiedContentModel], NotUsed] = mockStorageClientFunc
    override def getDetails(implicit timeout: FiniteDuration): Future[ClientDetails] =
      throw new NotImplementedError("Not implemented")
  }

  val mockRecommenderClientFunc: RecommenderRequest => RecommenderResponse = stub[RecommenderRequest => RecommenderResponse]
  val mockRecommenderClient: RecommenderClient = new RecommenderClient {
    val getRecommendationFor : Flow[RecommenderRequest, RecommenderResponse, NotUsed] = mockRecommenderClientFunc
    override def getDetails(implicit timeout: FiniteDuration): Future[ClientDetails]  =
      throw new NotImplementedError("Not Implemented")
  }

  val recommenderFlow = new RecommendArticleFlowImpl(
    storageClient     = mockStorageClient,
    recommenderClient = mockRecommenderClient
  )

  "Given the 'RecommendArticleFlow'" - {
    "it should be possible to get recommendations" - {
      "without applying any filter" in {
        mockRecommenderClientFunc.apply _ when sampleRecommenderRequest returns sampleRecommenderResponse once()
        mockStorageClientFunc.apply     _ when sampleContentIds         returns Seq(sampleContentObj1, sampleContentObj2) once()

        whenReady(recommenderFlow.getRecommendationFor(sampleRecommendArticle)){ res =>
          res shouldBe sampleRecommendArticleResult
        }
      }

      "filtering by range date" in {
        mockRecommenderClientFunc.apply _ when sampleRecommenderRequest returns sampleRecommenderResponse once()
        mockStorageClientFunc.apply     _ when Set(sampleContentId1)    returns Seq(sampleContentObj1)    once()

        whenReady(recommenderFlow.getRecommendationFor(sampleRecommendArticleWithDateRange)){ res =>
          res shouldBe RecommendArticleResult(Seq(sampleRecommendArticle_1))
        }
      }

      "filtering with offset and limit" in {
        mockRecommenderClientFunc.apply _ when sampleRecommenderRequest returns sampleRecommenderResponse once()
        mockStorageClientFunc.apply     _ when sampleContentIds         returns Seq(sampleContentObj1, sampleContentObj2) once()

        whenReady(recommenderFlow.getRecommendationFor(sampleRecommendArticleWithOffsetAndLimit)){ res =>
          res shouldBe RecommendArticleResult(Seq(sampleRecommendArticle_2))
        }
      }

      "filtering by Source" in {
        mockRecommenderClientFunc.apply _ when sampleRecommenderRequest returns sampleRecommenderResponse once()
        mockStorageClientFunc.apply     _ when sampleContentIds         returns Seq(sampleContentObj1, sampleContentObj2) once()

        whenReady(recommenderFlow.getRecommendationFor(sampleRecommendArticleWithSource)){ res =>
          res shouldBe RecommendArticleResult(Seq(sampleRecommendArticle_1))
        }
      }

      "filtering by Channel" in {
        mockRecommenderClientFunc.apply _ when sampleRecommenderRequest returns sampleRecommenderResponse once()
        mockStorageClientFunc.apply     _ when sampleContentIds         returns Seq(sampleContentObj1, sampleContentObj2) once()

        whenReady(recommenderFlow.getRecommendationFor(sampleRecommendArticleWithChannel)){ res =>
          res shouldBe RecommendArticleResult(Seq(sampleRecommendArticle_1))
        }
      }
    }

    "it should handle errors if" - {
      "the storage client fails" in {
        mockRecommenderClientFunc.apply _ when sampleRecommenderRequest throws sampleException once()

        whenReady(recommenderFlow.getRecommendationFor(sampleRecommendArticle).failed){ ex =>
          ex shouldBe sampleException
        }
      }

      "the recommender client fails" in {
        mockRecommenderClientFunc.apply _ when sampleRecommenderRequest returns sampleRecommenderResponse once()
        mockStorageClientFunc.apply _ when sampleContentIds throws sampleException once()

        whenReady(recommenderFlow.getRecommendationFor(sampleRecommendArticle).failed){ ex =>
          ex shouldBe sampleException
        }
      }
    }
  }
}

