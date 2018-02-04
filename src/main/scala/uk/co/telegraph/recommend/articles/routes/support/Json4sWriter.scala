package uk.co.telegraph.recommend.articles.routes.support

import akka.util.ByteString
import org.json4s.Extraction.decompose
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Formats}
import play.api.http.{MimeTypes, Writeable}

trait Json4sWriter {

  implicit def toJson[C](implicit formats:Formats = DefaultFormats): Writeable[C] = {
    def serialize(data: C): String = {
      JsonMethods.compact(decompose(data))
    }
    new Writeable[C]( data => ByteString.fromString(serialize(data)), Some(MimeTypes.JSON) )
  }
}
