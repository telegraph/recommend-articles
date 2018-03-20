package uk.co.telegraph.recommend.articles.routes.models

import java.time.ZonedDateTime

import uk.co.telegraph.recommend.articles.routes.models.RecommendArticlesRequest._

case class RecommendArticlesQueryFilter
(
  source     : Set[String],
  `date-from`: Option[ZonedDateTime],
  `date-to`  : Option[ZonedDateTime],
  channel    : Set[String]
){
  require( (`date-from`, `date-to`) match {
    case (Some(from), Some(to)) if from.isAfter(to) => false
    case _                                          => true
  }, "Invalid field - 'query-filters.date-from' must be before or equal 'query-filters.date-to'" )
}

case class RecommendArticlesQueryObject
(
  headline: Option[String],
  channel : Option[String],
  body    : String
){
  require( body.trim.length > 50, "Invalid field - 'query-object.body' length must be greater than 50")
}

case class RecommendArticlesRequest
(
  sort           : String                        = defaultSort,
  limit          : Int                           = defaultLimit,
  offset         : Int                           = defaultOffset,
  `query-filters`: RecommendArticlesQueryFilter = defaultQueryFilter,
  `query-object` : RecommendArticlesQueryObject
){
  require( limit  >  0, "Invalid field - 'limit' > 0" )
  require( offset >= 0, "Invalid field - 'offset' >= 0" )
  require( sortableFields.contains(sort), s"Invalid field - 'sort' must be one of the following values ${sortableFields.mkString("(",",",")")}" )
}

object RecommendArticlesRequest {

  val sortableFields = Seq(
    "-score",
    "+score"
  )
  val defaultLimit       = 5
  val defaultOffset      = 0
  val defaultSort        = "-score"
  val defaultQueryFilter = RecommendArticlesQueryFilter(
    source      = Set.empty,
    `date-from` = None,
    `date-to`   = None,
    channel     = Set.empty
  )
}
