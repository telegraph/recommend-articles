package uk.co.telegraph.recommend.articles

import java.time.format.DateTimeFormatter

package object utils {
  val zonedDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSSX")
}
