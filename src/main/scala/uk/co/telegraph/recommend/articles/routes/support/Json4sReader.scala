package uk.co.telegraph.recommend.articles.routes.support

import org.json4s.Formats
import org.json4s.jackson.JsonMethods
import play.api.mvc.{AbstractController, BodyParser}

import scala.concurrent.ExecutionContext

trait Json4sReader { this: AbstractController =>

  def json4sParser[T](implicit ec:ExecutionContext, formats:Formats, mf:Manifest[T]):BodyParser[T] =
    json4sParser(parse.DefaultMaxTextLength)

  def json4sParser[T](maxLength:Int)(implicit ec:ExecutionContext, formats:Formats, mf:Manifest[T]): BodyParser[T] =
    parse.json(maxLength)
      .map(_.toString())
      .map(payload => {
        JsonMethods.parse(payload).extract[T]
      })
}
