package uk.co.telegraph.recommend.articles.article

import io.restassured.module.scala.RestAssuredSupport._
import org.hamcrest.Matchers.hasItems
import uk.co.telegraph.recommend.articles.ComponentTest

class RecommendArticleSpecs extends ComponentTest
{
  info("")
  info("")
  info("As a User")
  info("I want to be able to get article recommendations")
  info("So it should be possible to call '/recommend-articles/by-article/'")
  info("")

  feature("/recommend-articles/by-article/") {
    scenario("should return article recommendations", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload.json")
      .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("data.id", hasItems ("11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222"))
    }

    scenario("should return the recommended articles and ignore errors on UCM Storage", ctTag) {
      storageComponent.setInvalidContentId("11111111-1111-1111-1111-111111111111")
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("data.id", hasItems ("22222222-2222-2222-2222-222222222222"))
    }
  }
}

