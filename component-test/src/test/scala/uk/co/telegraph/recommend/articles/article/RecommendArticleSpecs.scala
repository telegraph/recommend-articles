package uk.co.telegraph.recommend.articles.article

import io.restassured.module.scala.RestAssuredSupport._
import org.hamcrest.Matchers
import org.hamcrest.Matchers._
import org.hamcrest.Matchers.hasItem
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
        .body  ("result-count", equalTo(2))
        .body  ("data.size",    equalTo(2))

        .body  ("data.id[0]",         is ("11111111-1111-1111-1111-111111111111"))
        .body  ("data.score[0]",      is(0.9f))
        .body  ("data.type[0]",       is ("article"))
        .body  ("data.headline[0]",   is ("How to become a killer negotiator in nine steps"))
        .body  ("data.url[0]",        is ("https://www.telegraph.co.uk/business/2018/09/20/become-killer-negotiator-nine-steps/"))
        .body  ("data.thumbnail[0]",  is ("https://www.telegraph.co.uk/content/dam/business/2016/02/12/Mad-Men-boardroom_trans%2B%2BqVzuuqpFlyLIwiB6NTmJwfSVWeZ_vEN7c6bHu2jJnT8.jpg"))
        .body  ("data.pubdate[0]",    is ("2018-09-20T10:34:14.000Z"))
        .body  ("data.source[0]",     is ("AEM"))
        .body  ("data.authors[0]",    hasItem("Sophie Christie"))
        .body  ("data.authors[0].size", is(1))


        .body  ("data.id[1]",         is ("22222222-2222-2222-2222-222222222222"))
        .body  ("data.score[1]",      is(0.8f))
        .body  ("data.headline[1]",   is ("Sky battle lines drawn as Fox, Disney and Comcast prepare for £27bn auction"))
        .body  ("data.url[1]",        is ("https://www.telegraph.co.uk/business/2018/09/20/sky-battle-lines-drawn-fox-disney-comcast-prepare-27bn-auction/"))
        .body  ("data.thumbnail[1]",  is ("https://www.telegraph.co.uk/content/dam/business/2018/09/20/TELEMMGLPICT000175175560_trans%2B%2BpVlberWd9EgFPZtcLiMQfyf2A9a6I9YchsjMeADBa08.jpeg"))
        .body  ("data.pubdate[1]",    is ("2018-09-20T10:50:13.000Z"))
        .body  ("data.source[1]",     is ("pa"))
        .body  ("data.authors[1]",    contains("Christopher Williams", "Sophie Christie"))
        .body  ("data.authors[1].size",  is(2))
        .body  ("data.type[1]",       is ("article"))
    }

    scenario("should return article recommendations when specifying the limits and offset", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-offset-and-limit.json")
      .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", equalTo(1))
        .body  ("data.size",    equalTo(1))

        .body  ("data.id[0]",         is ("22222222-2222-2222-2222-222222222222"))
        .body  ("data.score[0]",      is(0.8f))
        .body  ("data.headline[0]",   is ("Sky battle lines drawn as Fox, Disney and Comcast prepare for £27bn auction"))
        .body  ("data.url[0]",        is ("https://www.telegraph.co.uk/business/2018/09/20/sky-battle-lines-drawn-fox-disney-comcast-prepare-27bn-auction/"))
        .body  ("data.thumbnail[0]",  is ("https://www.telegraph.co.uk/content/dam/business/2018/09/20/TELEMMGLPICT000175175560_trans%2B%2BpVlberWd9EgFPZtcLiMQfyf2A9a6I9YchsjMeADBa08.jpeg"))
        .body  ("data.pubdate[0]",    is ("2018-09-20T10:50:13.000Z"))
        .body  ("data.source[0]",     is ("pa"))
        .body  ("data.authors[0]",    contains("Christopher Williams", "Sophie Christie"))
        .body  ("data.authors[0].size",  is(2))
        .body  ("data.type[0]",       is ("article"))
    }

    scenario("should return article recommendations when filtering by source", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-filter-by-source.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", equalTo(1))
        .body  ("data.size",    equalTo(1))

        .body  ("data.id[0]",         is ("11111111-1111-1111-1111-111111111111"))
        .body  ("data.score[0]",      is(0.9f))
        .body  ("data.type[0]",       is ("article"))
        .body  ("data.headline[0]",   is ("How to become a killer negotiator in nine steps"))
        .body  ("data.url[0]",        is ("https://www.telegraph.co.uk/business/2018/09/20/become-killer-negotiator-nine-steps/"))
        .body  ("data.thumbnail[0]",  is ("https://www.telegraph.co.uk/content/dam/business/2016/02/12/Mad-Men-boardroom_trans%2B%2BqVzuuqpFlyLIwiB6NTmJwfSVWeZ_vEN7c6bHu2jJnT8.jpg"))
        .body  ("data.pubdate[0]",    is ("2018-09-20T10:34:14.000Z"))
        .body  ("data.source[0]",     is ("AEM"))
        .body  ("data.authors[0]",    hasItem("Sophie Christie"))
        .body  ("data.authors[0].size", is(1))
    }

    scenario("should return article recommendations when filtering by channel", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-filter-by-channel.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", equalTo(1))
        .body  ("data.size",    equalTo(1))

        .body  ("data.id[0]",         is ("22222222-2222-2222-2222-222222222222"))
        .body  ("data.score[0]",      is(0.8f))
        .body  ("data.headline[0]",   is ("Sky battle lines drawn as Fox, Disney and Comcast prepare for £27bn auction"))
        .body  ("data.url[0]",        is ("https://www.telegraph.co.uk/business/2018/09/20/sky-battle-lines-drawn-fox-disney-comcast-prepare-27bn-auction/"))
        .body  ("data.thumbnail[0]",  is ("https://www.telegraph.co.uk/content/dam/business/2018/09/20/TELEMMGLPICT000175175560_trans%2B%2BpVlberWd9EgFPZtcLiMQfyf2A9a6I9YchsjMeADBa08.jpeg"))
        .body  ("data.pubdate[0]",    is ("2018-09-20T10:50:13.000Z"))
        .body  ("data.source[0]",     is ("pa"))
        .body  ("data.authors[0]",    contains("Christopher Williams", "Sophie Christie"))
        .body  ("data.authors[0].size",  is(2))
        .body  ("data.type[0]",       is ("article"))
    }

    scenario("should return article recommendations when filtering by range-date", ctTag) {
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload-with-filter-by-range-date.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", equalTo(1))
        .body  ("data.size",    equalTo(1))

        .body  ("data.id[0]",         is ("22222222-2222-2222-2222-222222222222"))
        .body  ("data.score[0]",      is(0.8f))
        .body  ("data.headline[0]",   is ("Sky battle lines drawn as Fox, Disney and Comcast prepare for £27bn auction"))
        .body  ("data.url[0]",        is ("https://www.telegraph.co.uk/business/2018/09/20/sky-battle-lines-drawn-fox-disney-comcast-prepare-27bn-auction/"))
        .body  ("data.thumbnail[0]",  is ("https://www.telegraph.co.uk/content/dam/business/2018/09/20/TELEMMGLPICT000175175560_trans%2B%2BpVlberWd9EgFPZtcLiMQfyf2A9a6I9YchsjMeADBa08.jpeg"))
        .body  ("data.pubdate[0]",    is ("2018-09-20T10:50:13.000Z"))
        .body  ("data.source[0]",     is ("pa"))
        .body  ("data.authors[0]",    contains("Christopher Williams", "Sophie Christie"))
        .body  ("data.authors[0].size",  is(2))
        .body  ("data.type[0]",       is ("article"))
    }

    scenario("should return the recommended articles and ignore errors on UCM Storage", ctTag) {
      storageComponent.setInvalidContentId("11111111-1111-1111-1111-111111111111")
      serviceComponent.whenCallRecommendArticleFor("/service/request-recommend-article-payload.json")
        .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body  ("result-count", equalTo(1))
        .body  ("data.size",    equalTo(1))

        .body  ("data.id[0]",         is ("22222222-2222-2222-2222-222222222222"))
        .body  ("data.score[0]",      is(0.8f))
        .body  ("data.headline[0]",   is ("Sky battle lines drawn as Fox, Disney and Comcast prepare for £27bn auction"))
        .body  ("data.url[0]",        is ("https://www.telegraph.co.uk/business/2018/09/20/sky-battle-lines-drawn-fox-disney-comcast-prepare-27bn-auction/"))
        .body  ("data.thumbnail[0]",  is ("https://www.telegraph.co.uk/content/dam/business/2018/09/20/TELEMMGLPICT000175175560_trans%2B%2BpVlberWd9EgFPZtcLiMQfyf2A9a6I9YchsjMeADBa08.jpeg"))
        .body  ("data.pubdate[0]",    is ("2018-09-20T10:50:13.000Z"))
        .body  ("data.source[0]",     is ("pa"))
        .body  ("data.authors[0]",    contains("Christopher Williams", "Sophie Christie"))
        .body  ("data.authors[0].size",  is(2))
        .body  ("data.type[0]",       is ("article"))
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

