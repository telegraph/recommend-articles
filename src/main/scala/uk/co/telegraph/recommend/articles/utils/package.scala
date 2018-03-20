package uk.co.telegraph.recommend.articles

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

import scala.language.implicitConversions

package object utils {
  val defaultDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  implicit def stringToDateTime(dateTimeStr:String):ZonedDateTime = {
    LocalDateTime.parse(dateTimeStr, defaultDateTimeFormatter).atZone(ZoneId.of("UTC"))
  }

  implicit def dateTimeToString(dateTime:ZonedDateTime):String = {
    dateTime.format(defaultDateTimeFormatter)
  }

  val dateTimeSerializer = new CustomSerializer[ZonedDateTime](_ => (
    { case JString(dateStr) => stringToDateTime(dateStr) },
    PartialFunction.empty
  ))
}
