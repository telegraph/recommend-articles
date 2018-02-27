package uk.co.telegraph.recommend.articles.clients.storage

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import org.json4s.{DefaultFormats, Formats}
import play.api.Logger
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient._
import uk.co.telegraph.ucm.domain.UnifiedContentModel
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.http.impl.HttpClient
import uk.co.telegraph.utils.client.http.scaladsl.HttpClientImpl

trait StorageClient extends GenericClient{
  def getByIds:Flow[Set[ContentId], Seq[UnifiedContentModel], NotUsed]
}

trait HttpStorageClient
  extends HttpClient
  with StorageClient
{
  import uk.co.telegraph.utils.client.http.serialization.Json4sSupport._

  private val parallelism:Int = 10
  private implicit lazy val formats:Formats = DefaultFormats ++ UnifiedContentModel.serializers

  override lazy val getByIds: Flow[Set[ContentId], Seq[UnifiedContentModel], NotUsed] = Flow[Set[ContentId]]
    .mapConcat   ( _.toList )
    .flatMapMerge( parallelism, getById )
    .fold( Seq.empty[UnifiedContentModel] )(_ :+ _)

  private def getById(contentId: ContentId):Source[UnifiedContentModel, NotUsed] =
    Source.single( toHttpRequest(contentId) )
      .async
      .via( httpClientFlow.filterByStatus(OK).unmarshalTo[UnifiedContentModel] )
      .recoverWithRetries(-1 , {
        case ex:Throwable =>
          Logger.error("Failed to fetch UCM Data by Id", ex)
          Source.empty
      })


  private def toHttpRequest(contentId:ContentId):HttpRequest = {
    RequestBuilding.Get(s"${settings.baseUrl}/content/$contentId")
  }
}

object StorageClient{
  type ContentId = String

  val defaultConfigPath = "app.ucm-storage"

  def apply()(implicit system:ActorSystem, materializer: Materializer): StorageClient = {
    new HttpClientImpl(defaultConfigPath) with HttpStorageClient
  }
}
