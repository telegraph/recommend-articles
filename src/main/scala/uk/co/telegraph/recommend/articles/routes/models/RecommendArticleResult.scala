package uk.co.telegraph.recommend.articles.routes.models

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel(value = "RecommendArticleItem", description = "Recommend Article Item")
case class RecommendArticleItem
(
  @ApiModelProperty(value = "Article Id", name = "id", dataType = "string", required = true)
  id       : String,
  @ApiModelProperty(value = "Article Score", name = "score", dataType = "number", required = true)
  score    : Double,
  @ApiModelProperty(value = "Article Type", name = "type", dataType = "string", required = true)
  `type`   : String,
  @ApiModelProperty(value = "Article Headline", name = "headline", dataType = "string", required = true)
  headline : String,
  @ApiModelProperty(value = "Article Url", name = "url", dataType = "string", required = false)
  url      : Option[String],
  @ApiModelProperty(value = "Article's Thumbnail Url", name = "thumbnail", dataType = "string", required = false)
  thumbnail: Option[String],
  @ApiModelProperty(value = "Article's Publication Date", name = "pubdate", dataType = "string", required = false)
  pubdate  : Option[String],
  @ApiModelProperty(value = "Article's Channel", name = "channel", dataType = "string", required = false)
  channel  : Option[String],
  @ApiModelProperty(value = "Article's Source", name = "source", dataType = "string", required = true)
  source   : String,
  @ApiModelProperty(value = "Article's Authors", name = "authors", dataType = "string", required = true)
  authors  : Seq[String]
)

@ApiModel(value = "RecommendArticleResult", description = "Recommend Article Result", subTypes = Array(classOf[RecommendArticleItem]))
case class RecommendArticleResult
(
  @ApiModelProperty(value = "Recommendation result count", name = "result-count", dataType = "number", required = true)
  `result-count`: Int,
  @ApiModelProperty(value = "Recommendation result", name = "data", required = true)
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
