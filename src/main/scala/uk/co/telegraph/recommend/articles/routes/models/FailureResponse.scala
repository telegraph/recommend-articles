package uk.co.telegraph.recommend.articles.routes.models

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel(value = "FailureCause", description = "Object containing the error description")
case class FailureCause
(
  @ApiModelProperty(value = "Failure Cause Message", name = "message", dataType = "string", required = true)
  message: String,
  @ApiModelProperty(value = "Error Type",            name = "error", dataType = "string", required = true)
  error  : String
)

@ApiModel(value = "FailureResponse", description = "Object used described errors", subTypes = Array(classOf[FailureCause]))
case class FailureResponse
(
  @ApiModelProperty(value = "Failure Cause Message", name = "message", dataType = "string", required = true)
  message: String,
  @ApiModelProperty(value = "Error Type",            name = "error", dataType = "string", required = true)
  error  : String,
  @ApiModelProperty(value = "Nested Failure Causes", name = "causes")
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
      .toList

    FailureResponse(
      message   = exception.getMessage,
      error     = exception.getClass.getSimpleName,
      causes    = causes
    )
  }
}
