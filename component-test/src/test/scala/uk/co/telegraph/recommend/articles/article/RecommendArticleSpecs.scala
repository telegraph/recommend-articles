package uk.co.telegraph.recommend.articles.article

import io.restassured.module.scala.RestAssuredSupport._
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasItems
import uk.co.telegraph.recommend.articles.ComponentTest

class RecommendArticleSpecs extends ComponentTest
{
  info("")
  info("")
  info("As a User")
  info("I want to be able to get article recommendations")
  info("So it should be possible to call '/recommend-articles/by-article'")
  info("")

  feature("/recommend-articles/by-article") {
    scenario("should return article recommendations", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", Matchers.equalTo(2))
        .body  ("data.id", hasItems ("11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222"))
    }

    scenario("should return article recommendations when specifying the limits and offset", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-offset-and-limit.json")
      .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", Matchers.equalTo(1))
        .body  ("data.id",      hasItems ("22222222-2222-2222-2222-222222222222"))
    }

    scenario("should return article recommendations when filtering by source", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-filter-by-source.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", Matchers.equalTo(1))
        .body  ("data.id",      hasItems ("11111111-1111-1111-1111-111111111111"))
    }

    scenario("should return article recommendations when filtering by channel", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-filter-by-channel.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", Matchers.equalTo(1))
        .body  ("data.id",      hasItems ("22222222-2222-2222-2222-222222222222"))
    }

    scenario("should return article recommendations when filtering by range-date", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-filter-by-range-date.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", Matchers.equalTo(1))
        .body  ("data.id",      hasItems ("22222222-2222-2222-2222-222222222222"))
    }

    scenario("should return the recommended articles and ignore errors on UCM Storage", ctTag) {
      storageComponent.setInvalidContentId("11111111-1111-1111-1111-111111111111")
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", Matchers.equalTo(1))
        .body  ("data.id", hasItems ("22222222-2222-2222-2222-222222222222"))
    }

    scenario("should fail if the limit is 0", ctAndIt:_*){
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-invalid-limit.json")
        .Then()
        .statusCode(400)
    }

    scenario("should fail if the offset is -1", ctAndIt:_*){
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-invalid-offset.json")
        .Then()
        .statusCode(400)
    }

    scenario("should fail if invalid range date", ctAndIt:_*){
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-invalid-range-date.json")
        .Then()
        .statusCode(400)
    }

    scenario("should fail if invalid body length < 50", ctAndIt:_*){
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-invalid-body-length.json")
        .Then()
        .statusCode(400)
    }
  }
}

