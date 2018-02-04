package uk.co.telegraph.recommend.articles.clients.recommender.model


case class RecommenderItem
(
  `content-id`: String,
  score       : Double
)

case class RecommenderResponse
(
  `result-count`: Int,
  data          : Seq[RecommenderItem]
)
