package uk.co.telegraph.recommend.articles.utils

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, any, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.matching._
import com.github.tomakehurst.wiremock.stubbing._
import uk.co.telegraph.recommend.articles.environment.environmentConfig
import uk.co.telegraph.recommend.articles.utils.WiremockSupport._

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

trait WiremockSupport {

  private val host :String = "localhost"
  private val port :Int    = queryDockerPort("docker-compose port mock 8080")
    .getOrElse(environmentConfig.getInt("mock.port"))

  WireMock.configureFor(host, port)
  WireMock.reset()

  def proxyAll(urlPattern:String, toEndpoint:String)(implicit priority:Int = lowPriority):StubMapping = {
    any(urlMatching(urlPattern)).atPriority(priority).proxyTo(toEndpoint)
  }

  implicit def toMappingBuilderExtensions(left: MappingBuilder):MappingBuilderExtensions =
    MappingBuilderExtensions(left)
}

object WiremockSupport{

  val lowPriority = 10
  case class MappingBuilderExtensions(left:MappingBuilder){
    def willReplyWithStatusCode(status:Int): StubMapping = {
      left.willReturn(aResponse().withStatus(status))
    }

    def willReplyWithResponse(responseBuilder: ResponseDefinitionBuilder): StubMapping = {
      left.willReturn(responseBuilder)
    }

    def willReplyWithDelay(delay:FiniteDuration):StubMapping = {
      left.willReturn(aResponse().withFixedDelay(delay.toMillis.toInt).withStatus(200))
    }

    def bodyMatch(patterns: Seq[StringValuePattern]):MappingBuilder ={
      patterns.foldLeft(left)( _.withRequestBody(_) )
    }

    def proxyTo(toEndpoint:String):StubMapping = {
      left.willReturn( aResponse().proxiedFrom(toEndpoint) )
    }

    private implicit def toStubFor(mappingBuilder: MappingBuilder):StubMapping = {
      stubFor(mappingBuilder)
    }
  }

}
