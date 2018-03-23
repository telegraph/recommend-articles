package uk.co.telegraph.recommend.articles.flows

import java.time.ZonedDateTime

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient
import uk.co.telegraph.recommend.articles.clients.recommender.model.{RecommenderItem, RecommenderRequest}
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient
import uk.co.telegraph.recommend.articles.flows.RecommendArticleFlow._
import uk.co.telegraph.recommend.articles.models.{ArticleChannel, ArticleSource, QueryFilter, RecommendArticles}
import uk.co.telegraph.recommend.articles.routes.models.{RecommendArticleItem, RecommendArticleResult}
import uk.co.telegraph.recommend.articles.utils
import uk.co.telegraph.ucm.domain.{MetadataExtension, UnifiedContentModel}

import scala.concurrent.Future

trait RecommendArticleFlow {
  def getRecommendationFor(request:RecommendArticles):Future[RecommendArticleResult]
}

class RecommendArticleFlowImpl( private val storageClient: StorageClient, val recommenderClient: RecommenderClient )(implicit val mat:Materializer)
  extends RecommendArticleFlow
{

  private val storageEnrichmentFlow = Flow[Seq[RecommenderItem]]
    .flatMapConcat( itemSet => {
      val scoreMap = itemSet.map( item => (item.`content-id`, item.score)).toMap
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

  override def getRecommendationFor(request: RecommendArticles): Future[RecommendArticleResult] = {
    Source.single(request)
      .map(toRecommenderRequest)
      .via(recommenderClient.getRecommendationFor)
      .map(_.data)
      .map( preFilterData(request.queryFilter) )
      .via( storageEnrichmentFlow )
      .map( postFilterData(request.queryFilter) )
      .map( _.slice(request.offset, request.offset + request.limit) )
      .map( RecommendArticleResult(_) )
      .runWith(Sink.head)
  }

  def preFilterData(filter: QueryFilter): Seq[RecommenderItem] => Seq[RecommenderItem] = {
    _.filter( checkIncludedRange(filter.dateFrom, filter.dateTo) )
  }

  def postFilterData(filter: QueryFilter):Seq[RecommendArticleItem] => Seq[RecommendArticleItem] = {
    itemSeq =>itemSeq
      .filter(checkIncludesSource (filter.source))
      .filter(checkIncludesChannel(filter.channel))
      .sortBy(-_.score)
  }

  private def checkIncludedRange(fromOpt:Option[ZonedDateTime], toOpt:Option[ZonedDateTime]): RecommenderItem => Boolean = {
    item => {
      val fromDate = fromOpt.getOrElse(item.`date-last-modified`)
      val toDate   = toOpt  .getOrElse(item.`date-last-modified`)

      !(fromDate.isAfter(item.`date-last-modified`) || toDate.isBefore(item.`date-last-modified`))
    }
  }

  private def checkIncludesSource(articleSources: ArticleSource): RecommendArticleItem => Boolean = {
    articleSources match {
      case ArticleSource.All           => _ => true
      case ArticleSource.Only(sources) => item => sources.contains(item.source)
    }
  }

  private def checkIncludesChannel(articleChannel: ArticleChannel): RecommendArticleItem => Boolean = {
    articleChannel match {
      case ArticleChannel.All            => _ => true
      case ArticleChannel.Only(channels) => item => item.channel.exists(channels.contains(_))
    }
  }
}

object RecommendArticleFlow{
  private [flows] def toRecommenderRequest( request: RecommendArticles ): RecommenderRequest = {
    RecommenderRequest(
      channel  = request.queryObject.channel,
      headline = request.queryObject.headline,
      body     = request.queryObject.body
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
      pubdate   = ucmModel.metadata.source.flatMap(_.`created-date`).map(utils.dateTimeToString),
      channel   = ucmModel.metadata.extensions.collectFirst({
        case MetadataExtension("channel", channel) => channel
      }),
      source    = ucmModel.metadata.source.map(_.`source-id`).getOrElse("unknown"),
      authors   = ucmModel.content.authors.map(_.name)
    )
  }
}
