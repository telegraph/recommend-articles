package uk.co.telegraph.recommend.articles.models

import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.recommend.articles.routes.models.{RecommendArticlesQueryFilter, RecommendArticlesQueryObject, RecommendArticlesRequest}
import uk.co.telegraph.recommend.articles.routes.models.RecommendArticlesRequest._
import RecommendArticlesSpec._

class RecommendArticlesSpec
  extends FreeSpec
  with Matchers
{
  "Given the 'RecommendArticles'" - {
    "should be possible to create an object from" - {
      "filtering all channels and sections" in {
        RecommendArticles(sampleRecommendRequest_1) shouldBe sampleRecommend_1
      }

      "filtering some channels and sections" in {
        RecommendArticles(sampleRecommendRequest_2) shouldBe sampleRecommend_2
      }

      "with ascend sort" in {
        RecommendArticles(sampleRecommendRequest_3) shouldBe sampleRecommend_3
      }
    }
  }
}

object RecommendArticlesSpec {
  val sampleRecommendRequest_1 = RecommendArticlesRequest(
    sort   = defaultSort,
    limit  = defaultLimit,
    offset = defaultOffset,
    `query-filters` = RecommendArticlesQueryFilter(
      source        = Set.empty,
      `date-from`   = None,
      `date-to`     = None,
      channel       = Set.empty
    ),
    `query-object`  = RecommendArticlesQueryObject(
      headline      = None,
      channel       = None,
      body          = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )
  val sampleRecommendRequest_2 = RecommendArticlesRequest(
    sort   = defaultSort,
    limit  = defaultLimit,
    offset = defaultOffset,
    `query-filters` = RecommendArticlesQueryFilter(
      source        = Set("tmg"),
      `date-from`   = None,
      `date-to`     = None,
      channel       = Set("news")
    ),
    `query-object`  = RecommendArticlesQueryObject(
      headline      = None,
      channel       = None,
      body          = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )
  val sampleRecommendRequest_3 = RecommendArticlesRequest(
    sort   = "+score",
    limit  = defaultLimit,
    offset = defaultOffset,
    `query-filters` = RecommendArticlesQueryFilter(
      source        = Set.empty,
      `date-from`   = None,
      `date-to`     = None,
      channel       = Set.empty
    ),
    `query-object`  = RecommendArticlesQueryObject(
      headline      = None,
      channel       = None,
      body          = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )

  val sampleRecommend_1 = RecommendArticles(
    sort   = Sort("score", SortOrderEnum.desc),
    limit  = defaultLimit,
    offset = defaultOffset,
    queryFilter  = QueryFilter(
      source     = ArticleSource.All,
      dateFrom   = None,
      dateTo     = None,
      channel    = ArticleChannel.All
    ),
    queryObject  = QueryObject(
      headline   = None,
      channel    = None,
      body       = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )
  val sampleRecommend_2 = RecommendArticles(
    sort   = Sort("score", SortOrderEnum.desc),
    limit  = defaultLimit,
    offset = defaultOffset,
    queryFilter  = QueryFilter(
      source     = ArticleSource.Only(Seq("tmg")),
      dateFrom   = None,
      dateTo     = None,
      channel    = ArticleChannel.Only(Seq("news"))
    ),
    queryObject  = QueryObject(
      headline   = None,
      channel    = None,
      body       = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )
  val sampleRecommend_3 = RecommendArticles(
    sort   = Sort("score", SortOrderEnum.asc),
    limit  = defaultLimit,
    offset = defaultOffset,
    queryFilter  = QueryFilter(
      source     = ArticleSource.All,
      dateFrom   = None,
      dateTo     = None,
      channel    = ArticleChannel.All
    ),
    queryObject  = QueryObject(
      headline   = None,
      channel    = None,
      body       = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )
}
