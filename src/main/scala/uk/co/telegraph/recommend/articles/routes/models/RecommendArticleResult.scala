package uk.co.telegraph.recommend.articles.routes.models

import io.swagger.annotations.{ApiModel, ApiModelProperty}

case class RecommendArticleItem
(
  id       : String,
  score    : Double,
  `type`   : String,
  headline : String,
  url      : Option[String],
  thumbnail: Option[String],
  pubdate  : Option[String],
  channel  : Option[String],
  source   : String,
  authors  : Seq[String]
)

case class RecommendArticleResult
(
  `result-count`: Int,
  data          : Seq[RecommendArticleItem]
)

object RecommendArticleResult {
  def apply(items:Seq[RecommendArticleItem]): RecommendArticleResult = {
    RecommendArticleResult(
      `result-count` = items.length,
      data           = items
    )
  }
}
