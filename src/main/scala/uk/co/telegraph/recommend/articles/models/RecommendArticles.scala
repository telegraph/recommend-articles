package uk.co.telegraph.recommend.articles.models

import java.time.ZonedDateTime

import uk.co.telegraph.recommend.articles.models.SortOrderEnum.SortOrderType
import uk.co.telegraph.recommend.articles.routes.models.{RecommendArticlesQueryFilter, RecommendArticlesQueryObject, RecommendArticlesRequest}

object SortOrderEnum extends Enumeration{
  type SortOrderType = Value
  val asc : SortOrderType = Value("asc")
  val desc: SortOrderType = Value("desc")
}
case class Sort( field: String, order: SortOrderType)

case class QueryFilter
(
  source  : ArticleSource,
  channel : ArticleChannel,
  dateFrom: Option[ZonedDateTime],
  dateTo  : Option[ZonedDateTime]
)

case class QueryObject
(
  headline: Option[String],
  channel : Option[String],
  body    : String
)

case class RecommendArticles
(
  sort       : Sort,
  limit      : Int,
  offset     : Int,
  queryFilter: QueryFilter,
  queryObject: QueryObject
)


object RecommendArticles {

  private def toSort(sortRaw:String):Sort = {
    val order = if (sortRaw.startsWith("-")) SortOrderEnum.desc else SortOrderEnum.asc

    Sort(
      field = sortRaw.substring(1),
      order = order
    )
  }

  private def toQueryFilter(request: RecommendArticlesQueryFilter): QueryFilter = {
    QueryFilter(
      source   = ArticleSource(request.source),
      channel  = ArticleChannel(request.channel),
      dateFrom = request.`date-from`,
      dateTo   = request.`date-to`
    )
  }

  def toQueryObject(queryObject: RecommendArticlesQueryObject): QueryObject = {
    QueryObject(
      headline = queryObject.headline,
      channel  = queryObject.channel,
      body     =  queryObject.body
    )
  }

  def apply(request: RecommendArticlesRequest):RecommendArticles = {
    RecommendArticles(
      sort        = toSort(request.sort),
      limit       = request.limit,
      offset      = request.offset,
      queryFilter = toQueryFilter(request.`query-filters`),
      queryObject = toQueryObject(request.`query-object`)
    )
  }
}
