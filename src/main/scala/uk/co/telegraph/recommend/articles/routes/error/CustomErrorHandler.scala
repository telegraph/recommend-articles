package uk.co.telegraph.recommend.articles.routes.error

import javax.inject.Singleton
import org.json4s.{DefaultFormats, Formats, MappingException}
import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import uk.co.telegraph.recommend.articles.routes.models.FailureResponse
import uk.co.telegraph.recommend.articles.routes.support.Json4sWriter

import scala.concurrent._

@Singleton
class CustomErrorHandler extends HttpErrorHandler with Json4sWriter {

  implicit val formats: Formats = DefaultFormats

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    Future.successful(
      ex match {
        case ex @ (_:IllegalArgumentException | _:MappingException)=>
          val response = FailureResponse(ex)
          Logger.error(s"Returning Error: '${ex.getMessage}'. Response: $response.", ex)
          BadRequest(response)
        case ex:Throwable =>
          val response = FailureResponse(ex)
          Logger.error(s"Returning Error: '${ex.getMessage}'. Response: $response.", ex)
          InternalServerError(response)
      }
    )
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)(FailureResponse(message, "", Seq()))
    )
  }
}
