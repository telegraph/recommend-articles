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
  }

  override def setInvalidContentId(contentId:String): Unit = {
    get(urlPathEqualTo(s"/ucm-storage-service/content/$contentId"))
      .atPriority(1)
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
