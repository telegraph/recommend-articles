package uk.co.telegraph.recommend.articles.clients.recommender.model

case class RecommenderRequest
(
  fields  : Seq[String]   = RecommenderRequest.defaultFields,
  channel : Option[String],
  headline: Option[String],
  body    : String
)

object RecommenderRequest {
  val defaultFields:Seq[String] = Seq("content-id", "score", "weight", "date-last-modified")

}
