package uk.co.telegraph.recommend.articles.routes.models

case class FailureResponseStatus
(
  statusCode  : Int,
  appErrorCode: Int,
  message     : String
)

case class FailureResponse
(
  status: FailureResponseStatus
)

object FailureResponse{

  def apply(exception: Throwable): FailureResponse = {
    FailureResponse(
      status = FailureResponseStatus(
        message      = exception.getMessage,
        statusCode   = 500,
        appErrorCode = 1500
      )
    )
  }

  def apply(statusCode:Int, appErrorCode:Int, message:String): FailureResponse = {
    FailureResponse(
      status = FailureResponseStatus(
        message      = message,
        statusCode   = statusCode,
        appErrorCode = appErrorCode
      )
    )
  }
}
