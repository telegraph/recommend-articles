package uk.co.telegraph.recommend.articles.routes.models

import org.json4s.{CustomSerializer, Serializer}
import uk.co.telegraph.recommend.articles.routes.models.SortOrderEnum.SortOrderType

object SortOrderEnum extends Enumeration {
  type SortOrderType = Value

  val asc :SortOrderType = Value
  val desc:SortOrderType = Value
}

case class Sort(field:String, order:SortOrderType)
case class RecommendArticleQueryFilter
(
  source     : Set[String],
  `date-from`: Option[String],
  `date-to`  : Option[String],
  channel    : Option[String]
)
case class RecommendArticleQueryObject
(
  headline: String,
  channel : String,
  body    : String
)

case class RecommendArticleRequest
(
  sort           : Sort,
  limit          : Int,
  offset         : Int,
  `query-filters`: RecommendArticleQueryFilter,
  `query-object` : RecommendArticleQueryObject
)

object RecommendArticleRequest{

  object SortSerializer extends CustomSerializer[Sort]( implicit ser => ({
    case jsonAST =>
      val sortWithOrder = jsonAST.extract[String]

      if( sortWithOrder.startsWith("-") ){
        val field = sortWithOrder.substring(1)
        Sort(field = field, order = SortOrderEnum.desc)
      }else {
        val field = if( sortWithOrder.startsWith("+") ) sortWithOrder.substring(1) else sortWithOrder
        Sort(field = field, order = SortOrderEnum.asc)
      }
  }, PartialFunction.empty))

  val serializers: Seq[Serializer[_]] = Seq(SortSerializer)
}
