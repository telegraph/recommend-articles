package uk.co.telegraph.recommend.articles

import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest._
import uk.co.telegraph.recommend.articles.components._
import uk.co.telegraph.recommend.articles.environment._

trait ComponentTest
  extends FeatureSpec
  with BeforeAndAfter
  with BeforeAndAfterAll
  with OneInstancePerTest
{
  //noinspection TypeAnnotation
  val serviceComponent = ServiceComponent
  val (storageComponent, recommenderComponent) = { if(componentTestMode)
      (StorageComponentCt, RecommenderEngineComponentCt)
    else
      (StorageComponentIt, RecommenderEngineComponentIt)
  }

  val ctTag  : Tag = Tag("ct")
  val itTag  : Tag = Tag("it")
  val ctAndIt: Seq[Tag] = Seq(ctTag, itTag)

  before{
    if(componentTestMode){
      WireMock.reset()
    }
    serviceComponent    .setup()
    storageComponent    .setup()
    recommenderComponent.setup()
  }

  after{
    storageComponent    .teardown()
    serviceComponent    .teardown()
    recommenderComponent.teardown()
  }
}
