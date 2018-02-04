package uk.co.telegraph.recommend.articles.clients

import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient.ContentId
import uk.co.telegraph.recommend.articles.clients.storage.{HttpStorageClient, StorageClient}
import uk.co.telegraph.recommend.articles.{TestContext, TestDataStorageClient}
import uk.co.telegraph.utils.client.HttpClientHelper
import uk.co.telegraph.utils.client.http.impl.HttpClient

class StorageClientSpecs
  extends FreeSpec
  with TestContext
  with MockFactory
  with Matchers
  with HttpClientHelper
  with TestDataStorageClient
{

  val client:StorageClient = new HttpClientMock(StorageClient.defaultConfigPath) with HttpStorageClient

  "Given a 'StorageClient'" - {
    "it should be an Http Client" in {
      StorageClient() shouldBe a [HttpClient]
    }

    "when invoked 'getByIds'" - {
      "it should return UCM Objects" in {
        HttpConnectorMock.doRequest _ when sampleGetByIdRequestContent1 returns sampleGetByIdResponseContent1 once()
        HttpConnectorMock.doRequest _ when sampleGetByIdRequestContent2 returns sampleGetByIdResponseContent2 once()

        val (pub, sub) = TestSource.probe[Set[ContentId]]
          .via(client.getByIds)
          .toMat(TestSink.probe)(Keep.both)
          .run()

        sub.request(1)
        pub.sendNext(sampleContentIds)
        pub.sendComplete()
        sub.expectNext().sortBy(_.metadata.`content-id`) shouldBe sampleContentItems.sortBy(_.metadata.`content-id`)
        sub.expectComplete()
      }

      "it should skip failed items" in {
        HttpConnectorMock.doRequest _ when sampleGetByIdRequestContent1 returns sampleGetByIdResponseContent1 once()
        HttpConnectorMock.doRequest _ when sampleGetByIdRequestContent2 returns sampleNotFoundResponse once()

        val (pub, sub) = TestSource.probe[Set[ContentId]]
          .via(client.getByIds)
          .toMat(TestSink.probe)(Keep.both)
          .run()

        sub.request(1)
        pub.sendNext(sampleContentIds)
        pub.sendComplete()
        sub.expectNext(Seq(sampleContentObj1))
        sub.expectComplete()
      }
    }
  }
}
