package uk.co.telegraph.recommend.articles.clients.recommender.model

import java.time.ZonedDateTime


case class RecommenderItem
(
  `content-id`    : String,
  score           : Double,
  `date-published`: ZonedDateTime
)

case class RecommenderResponse
(
  `result-count`: Int,
  data          : Seq[RecommenderItem]
)
