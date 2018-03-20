package uk.co.telegraph.recommend.articles.routes.models

import uk.co.telegraph.recommend.articles.utils._

import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.recommend.articles.routes.models.RecommendArticlesRequest._

class RecommendArticlesRequestSpecs
  extends FreeSpec
  with Matchers
{
  "Given the 'RecommendArticlesRequest'"- {
    "should fail with" - {
      "sort shouldbe allowed" in {
        val result = intercept[IllegalArgumentException](
          RecommendArticlesRequest(
            sort = "~score",
            limit = defaultLimit,
            offset = defaultOffset,
            `query-filters` = defaultQueryFilter,
            `query-object` = RecommendArticlesQueryObject(
              channel  = None,
              headline = None,
              body     = "Fake Article body - This body is for test purposes only and should not be used in production"
            )
          )
        )
        result.getMessage shouldBe "requirement failed: Invalid field - 'sort' must be one of the following values (-score,+score)"
      }

      "limit <= 0" in {
        val result = intercept[IllegalArgumentException](
          RecommendArticlesRequest(
            sort = defaultSort,
            limit = 0,
            offset = defaultOffset,
            `query-filters` = defaultQueryFilter,
            `query-object` = RecommendArticlesQueryObject(
              channel  = None,
              headline = None,
              body     = "Fake Article body - This body is for test purposes only and should not be used in production"
            )
          )
        )
        result.getMessage shouldBe "requirement failed: Invalid field - 'limit' > 0"
      }

      "offset < 0" in {
        val result = intercept[IllegalArgumentException](
          RecommendArticlesRequest(
            sort = defaultSort,
            limit = defaultLimit,
            offset = -1,
            `query-filters` = defaultQueryFilter,
            `query-object` = RecommendArticlesQueryObject(
              channel  = None,
              headline = None,
              body     = "Fake Article body - This body is for test purposes only and should not be used in production"
            )
          )
        )
        result.getMessage shouldBe "requirement failed: Invalid field - 'offset' >= 0"
      }

      "query-object.body length < 50" in {
        val result = intercept[IllegalArgumentException](
          RecommendArticlesRequest(
            sort = defaultSort,
            limit = defaultLimit,
            offset = defaultOffset,
            `query-filters` = defaultQueryFilter,
            `query-object` = RecommendArticlesQueryObject(
              channel  = None,
              headline = None,
              body     = "Fake Article body - short body"
            )
          )
        )
        result.getMessage shouldBe "requirement failed: Invalid field - 'query-object.body' length must be greater than 50"
      }

      "query-filters.date-from > query-filters.date-to" in {
        val result = intercept[IllegalArgumentException](
          RecommendArticlesRequest(
            sort = defaultSort,
            limit = defaultLimit,
            offset = defaultOffset,
            `query-filters` = RecommendArticlesQueryFilter(
              source     = Set.empty,
              `date-from`= Some( "2017-01-01T12:00:00.000Z" ),
              `date-to`  = Some( "2016-01-01T12:00:00.000Z" ),
              channel    = Set.empty
            ),
            `query-object` = RecommendArticlesQueryObject(
              channel  = None,
              headline = None,
              body     = "Fake Article body - This body is for test purposes only and should not be used in production"
            )
          )
        )
        result.getMessage shouldBe "requirement failed: Invalid field - 'query-filters.date-from' must be before or equal 'query-filters.date-to'"
      }
    }
  }
}

