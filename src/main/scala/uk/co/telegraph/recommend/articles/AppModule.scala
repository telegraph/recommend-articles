package uk.co.telegraph.recommend.articles

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Key.get
import com.google.inject.multibindings.Multibinder.newSetBinder
import com.google.inject.{AbstractModule, Provides}
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient
import uk.co.telegraph.recommend.articles.flows.{RecommendArticleFlow, RecommendArticleFlowImpl}
import uk.co.telegraph.utils.client.GenericClient

class AppModule extends AbstractModule{

  override def configure(): Unit = {
    val genericClients = newSetBinder(binder(), get(classOf[GenericClient]))
    //TODO: Set here the clients to be monitored
    genericClients.addBinding().to(classOf[RecommenderClient])
    genericClients.addBinding().to(classOf[StorageClient])
  }

  //TODO: Create different client providers here!
  @Provides
  def storageClientProvider(implicit system:ActorSystem, materializer:Materializer):StorageClient =
    StorageClient()

  @Provides
  def recommenderClientProvider(implicit system:ActorSystem, materializer:Materializer):RecommenderClient =
    RecommenderClient()

  @Provides
  def recommendArticleFlowProvider(storageClient: StorageClient, recommenderClient: RecommenderClient)(implicit materializer: Materializer):RecommendArticleFlow =
    new RecommendArticleFlowImpl(storageClient, recommenderClient)
}
