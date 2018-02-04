package uk.co.telegraph.recommend.articles.routes

import javax.inject.Inject

import io.swagger.annotations._
import org.json4s.{DefaultFormats, Formats}
import play.api.http.MimeTypes
import play.api.mvc._
import uk.co.telegraph.recommend.articles.flows.RecommendArticleFlow
import uk.co.telegraph.recommend.articles.routes.models.{FailureResponse, RecommendArticleRequest, RecommendArticleResult}
import uk.co.telegraph.recommend.articles.routes.support.{Json4sReader, Json4sWriter}

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
  implicit val formats:Formats = DefaultFormats ++ RecommendArticleRequest.serializers

  @ApiOperation(value = "Endpoint used to get article recommendations")
  @ApiResponses(Array(
    new ApiResponse(
      code     = 200,
      message  = "List of recommendations",
      response = classOf[RecommendArticleResult]
    ),
    new ApiResponse(
      code     = 500,
      message  = "Returned an exception/error is thrown during flow execution",
      response = classOf[FailureResponse]
    )
  ))
  def recommendArticles(): Action[RecommendArticleRequest] = Action(json4sParser[RecommendArticleRequest]).async { implicit request =>
    recommendedArticleFlow.getRecommendationFor(request.body)
      .map( result => {
        Ok(result).as(MimeTypes.JSON)
      })
  }
}
