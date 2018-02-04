package uk.co.telegraph.recommend.articles.components

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.co.telegraph.recommend.articles.utils.{WiremockSupport, fromPayload}

abstract class RecommenderComponent extends Component("recommender") {
  def setOffline(): Unit = {}

  def setOnline(): Unit = {}
}

object RecommenderComponentCt extends RecommenderComponent with WiremockSupport{
  override def setup(): Unit = {
    teardown()

    get(urlPathEqualTo("/recommender/health"))
      .willReplyWithStatusCode(200)

    post(urlPathEqualTo("/recommender/article"))
      .willReplyWithResponse(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(fromPayload("/recommender/payload-recommender.json"))
      )
  }

  override def teardown(): Unit = {
  }
}

object RecommenderComponentIt extends RecommenderComponent{

}
