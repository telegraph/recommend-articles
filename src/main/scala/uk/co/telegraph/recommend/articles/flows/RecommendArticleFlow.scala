package uk.co.telegraph.recommend.articles.flows

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient
import uk.co.telegraph.recommend.articles.clients.recommender.model.RecommenderRequest
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient
import uk.co.telegraph.recommend.articles.flows.RecommendArticleFlow._
import uk.co.telegraph.recommend.articles.routes.models.{RecommendArticleItem, RecommendArticleRequest, RecommendArticleResult}
import uk.co.telegraph.recommend.articles.utils.zonedDateTimeFormatter
import uk.co.telegraph.ucm.domain.{MetadataExtension, UnifiedContentModel}

import scala.concurrent.Future

trait RecommendArticleFlow {
  def getRecommendationFor(request:RecommendArticleRequest):Future[RecommendArticleResult]
}

class RecommendArticleFlowImpl(private val storageClient: StorageClient, val recommenderClient: RecommenderClient )(implicit val mat:Materializer) extends RecommendArticleFlow {
  override def getRecommendationFor(request: RecommendArticleRequest): Future[RecommendArticleResult] = {
    Source.single(request)
      .map(toRecommenderRequest)
      .via(recommenderClient.getRecommendationFor)
      .flatMapConcat( recommended => {
        val scoreMap = recommended.data.map( item => (item.`content-id`, item.score)).toMap

        Source.single(scoreMap.keySet)
          .via       ( storageClient.getByIds )
          .mapConcat (_.toList)
          .map       ( item => {
            val contentId = item.metadata.`content-id`
            val score     = scoreMap.getOrElse( contentId, 0.0 )
            toRecommenderResult(item, score)
          })
          .fold(Seq.empty[RecommendArticleItem])(_:+_)
      })
      .map( RecommendArticleResult(_) )
      .runWith(Sink.head)
  }
}

object RecommendArticleFlow{

  private [flows] def toRecommenderRequest( recommendArticleRequest: RecommendArticleRequest ): RecommenderRequest = {
    RecommenderRequest(
      channel  = recommendArticleRequest.`query-object`.channel,
      headline = recommendArticleRequest.`query-object`.headline,
      body     = recommendArticleRequest.`query-object`.body.toLowerCase
    )
  }

  private [flows] def toRecommenderResult( ucmModel: UnifiedContentModel, score:Double ): RecommendArticleItem = {
    RecommendArticleItem(
      id        = ucmModel.metadata.`content-id`,
      score     = score,
      headline  = ucmModel.content.headline,
      `type`    = ucmModel.metadata.`type`.toString,
      url       = ucmModel.metadata.extensions.collectFirst({
        case MetadataExtension("url", url) => url
      }),
      thumbnail = None,
      pubdate   = ucmModel.metadata.source.flatMap(_.`created-date`).map(_.format(zonedDateTimeFormatter)),
      channel   = ucmModel.metadata.extensions.collectFirst({
        case MetadataExtension("channel", channel) => channel
      }),
      source    = ucmModel.metadata.source.map(_.`source-id`).getOrElse("unknown"),
      authors   = ucmModel.content.authors.map(_.name)
    )
  }
}
