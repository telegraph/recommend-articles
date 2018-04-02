package uk.co.telegraph.recommend.articles.components

import uk.co.telegraph.recommend.articles.utils.WiremockSupport

abstract class RecommenderEngineComponent extends Component("recommender") {
  def setOffline(): Unit = {}

  def setOnline(): Unit = {}
}

object RecommenderEngineComponentCt extends RecommenderEngineComponent with WiremockSupport{
  override def setup(): Unit = {
    teardown()
  }

  override def teardown(): Unit = {
  }
}

object RecommenderEngineComponentIt extends RecommenderEngineComponent{

}
