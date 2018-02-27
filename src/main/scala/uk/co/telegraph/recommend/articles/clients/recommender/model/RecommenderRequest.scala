package uk.co.telegraph.recommend.articles.clients.recommender.model

case class RecommenderRequest
(
  channel : String,
  headline: String,
  body    : String
)
