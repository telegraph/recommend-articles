package uk.co.telegraph.recommend.articles.components

import io.restassured.response.Response
import uk.co.telegraph.recommend.articles.utils._

object ServiceComponent extends Component("app") {

  def whenCallDocumentation(format: String):Response = {
    given()
      .queryParam("format", format)
      .get("/swagger")
  }

  def whenCallHealthEndpoint(): Response = {
    given()
      .queryParam("cached", "false")
      .get("/health")
  }

  def whenCallRecommendArticleFor(payload:String):Response = {
    given()
      .body(fromPayload(payload))
      .contentType("application/json")
      .post("/recommend-articles/by-article/")
  }
}
