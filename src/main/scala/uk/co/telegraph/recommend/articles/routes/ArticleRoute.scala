package uk.co.telegraph.recommend.articles.routes

import javax.inject.Inject

import org.json4s.{DefaultFormats, Formats}
import play.api.http.MimeTypes
import play.api.mvc._
import uk.co.telegraph.recommend.articles.flows.RecommendArticleFlow
import uk.co.telegraph.recommend.articles.models.RecommendArticles
import uk.co.telegraph.recommend.articles.routes.models.RecommendArticlesRequest
import uk.co.telegraph.recommend.articles.routes.support.{Json4sReader, Json4sWriter}
import uk.co.telegraph.recommend.articles.utils._

import scala.concurrent.ExecutionContext

class ArticleRoute @Inject()
(
  controllerComponents  : ControllerComponents,
  recommendedArticleFlow: RecommendArticleFlow
)(implicit executionContext: ExecutionContext)
  extends AbstractController(controllerComponents)
  with Json4sWriter
  with Json4sReader
{
  implicit val formats:Formats = DefaultFormats + dateTimeSerializer

  def recommendArticles(): Action[RecommendArticlesRequest] = Action(json4sParser[RecommendArticlesRequest]).async { implicit request =>
    recommendedArticleFlow.getRecommendationFor(RecommendArticles(request.body))
      .map( result => {
        Ok(result).as(MimeTypes.JSON)
      })
  }
}
