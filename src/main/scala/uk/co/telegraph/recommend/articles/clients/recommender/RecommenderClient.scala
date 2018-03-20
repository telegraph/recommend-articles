package uk.co.telegraph.recommend.articles.clients.recommender

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import org.json4s.{DefaultFormats, Formats}
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient.GetRecommendationException
import uk.co.telegraph.recommend.articles.clients.recommender.model.{RecommenderRequest, RecommenderResponse}
import uk.co.telegraph.recommend.articles.utils
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.exceptions.ClientException
import uk.co.telegraph.utils.client.http.impl.HttpClient
import uk.co.telegraph.utils.client.http.scaladsl.HttpClientImpl
import uk.co.telegraph.utils.client.http.serialization.Json4sSupport

import scala.language.implicitConversions

trait RecommenderClient extends GenericClient{
  def getRecommendationFor:Flow[RecommenderRequest, RecommenderResponse, NotUsed]
}

trait HttpRecommenderClient
  extends HttpClient
  with RecommenderClient
{
  import Json4sSupport._
  import system.dispatcher
  private lazy implicit val formats:Formats = DefaultFormats + utils.dateTimeSerializer

  override lazy val getRecommendationFor:Flow[RecommenderRequest, RecommenderResponse, NotUsed] = Flow[RecommenderRequest]
    .flatMapConcat( request => {
      Source.single( toHttpRequest(request) )
        .via(httpClientFlow.filterByStatus(OK).unmarshalTo[RecommenderResponse])
        .mapError({
          case ex:Throwable =>
            GetRecommendationException(s"Failed to get recommendation for request $request", ex)
        })
    })

  private def toHttpRequest(request:RecommenderRequest):HttpRequest = {
    Post(s"${settings.baseUrl}/article", request)
  }
}


object RecommenderClient{
  val defaultConfigPath = "app.recommender"

  case class GetRecommendationException(message:String, cause:Throwable) extends ClientException(message, cause)

  def apply()(implicit system:ActorSystem, materializer: Materializer): RecommenderClient = {
    new HttpClientImpl(defaultConfigPath) with HttpRecommenderClient
  }
}
