package uk.co.telegraph.recommend.articles.routes.models

case class FailureCause
(
  message: String,
  error  : String
)

case class FailureResponse
(
  message: String,
  error  : String,
  causes : Seq[FailureCause]
)

object FailureResponse{

  private def toFailureCause(ex:Throwable):FailureCause = {
    FailureCause(ex.getMessage, ex.getClass.getSimpleName)
  }

  def apply(exception: Throwable): FailureResponse = {
    val causes = Iterator.iterate(exception.getCause)(_.getCause)
      .takeWhile(Option(_).nonEmpty)
      .map      ( toFailureCause )
      .filter   ( _.message != null)
      .toList

    FailureResponse(
      message   = exception.getMessage,
      error     = exception.getClass.getSimpleName,
      causes    = causes
    )
  }
}
