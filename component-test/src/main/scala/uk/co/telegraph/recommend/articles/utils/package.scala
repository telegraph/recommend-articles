package uk.co.telegraph.recommend.articles

import scala.io.{Codec, Source}
import scala.sys.process._

package object utils {
  def fromPayload(name:String):String = {
    Source.fromInputStream( getClass.getResourceAsStream(name) )(Codec.UTF8)
      .getLines()
      .mkString("", "\n", "\n")
  }

  def queryDockerPort(command:String):Option[Int] = {
    val commandResult = command.!!
    commandResult.split(':').lastOption.map(_.trim.toInt)
  }
}
