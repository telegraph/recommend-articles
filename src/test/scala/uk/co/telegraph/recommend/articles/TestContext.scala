package uk.co.telegraph.recommend.articles

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import com.typesafe.config.Config
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.language.implicitConversions

trait TestContext
  extends BeforeAndAfterAll
{ this:Suite =>

  lazy val testConfig      :Config       = TestData.testDefaultConfig
  implicit lazy val testActorSystem :ActorSystem       = ActorSystem("test-system", testConfig)
  implicit lazy val testMaterializer:ActorMaterializer = ActorMaterializer()

  override def afterAll():Unit = {
    testMaterializer.shutdown()
    testActorSystem.terminate()
  }
}

object TestContext{

  implicit def functionToFlow[A, B]( fnc: A => B):Flow[A, B, NotUsed] =
    Flow[A].flatMapConcat( input => {
      try {
        val output = fnc(input)
        Source.single(output)
      }catch {
        case ex:Exception =>
          Source.failed(ex)
      }
    })
}
