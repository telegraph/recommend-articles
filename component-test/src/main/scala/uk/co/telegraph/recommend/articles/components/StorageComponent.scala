package uk.co.telegraph.recommend.articles.components

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.co.telegraph.recommend.articles.utils._

import scala.concurrent.duration._
import scala.language.postfixOps

abstract class StorageComponent extends Component("storage") {

  def setOffline(): Unit = {}

  def setOnline(): Unit = {}

  def setInvalidContentId(contentId:String): Unit = {}
}

object StorageComponentCt extends StorageComponent with WiremockSupport{
  override def setup(): Unit = {
    teardown()

    get(urlPathEqualTo("/ucm-storage-service/health"))
      .willReplyWithStatusCode(200)

    get(urlPathEqualTo("/ucm-storage-service/content/11111111-1111-1111-1111-111111111111"))
      .willReplyWithResponse(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(fromPayload("/storage/payload-content1.json"))
      )

    get(urlPathEqualTo("/ucm-storage-service/content/22222222-2222-2222-2222-222222222222"))
      .willReplyWithResponse(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(fromPayload("/storage/payload-content2.json"))
      )
  }

  override def setInvalidContentId(contentId:String): Unit = {
    get(urlPathEqualTo(s"/ucm-storage-service/content/$contentId"))
      .willReplyWithStatusCode(404)
  }

  override def setOffline(): Unit = {
    any(urlPathMatching(s"/.*")).willReplyWithDelay( 10 seconds)
  }

  override def setOnline(): Unit = {
  }
}

object StorageComponentIt extends StorageComponent{

}
