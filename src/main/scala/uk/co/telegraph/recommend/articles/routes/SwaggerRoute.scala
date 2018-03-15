package uk.co.telegraph.recommend.articles.routes

import java.io.InputStream
import java.util.{Map => JMap}
import javax.inject.Inject

import com.fasterxml.jackson.databind.ObjectMapper
import org.yaml.snakeyaml.Yaml
import play.api.http.ContentTypes
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import uk.co.telegraph.recommend.articles.routes.SwaggerRoute._

import scala.io.Source

class SwaggerRoute @Inject()
(
  controllerComponents  : ControllerComponents
)
  extends AbstractController(controllerComponents){

  private lazy val jsonSerializer = new ObjectMapper()
  private lazy val yamlDeserializer = new Yaml()

  def swaggerDoc(format:String = "yaml"):Action[AnyContent] = Action { _ =>
    Option(format) match {
      case Some("json") => swaggerAsJson()
      case _            => swaggerAsYaml()
    }
  }

  private def swaggerAsYaml() = {
    val payload = Source.fromInputStream(loadDocumentation())
      .getLines()
      .mkString("\n")
    Ok(payload).as("text/yaml")
  }

  private def swaggerAsJson() = {
    val yamlPayload = yamlDeserializer.load[YamlObject](loadDocumentation())
    val payload     = jsonSerializer.writeValueAsString(yamlPayload)
    Ok(payload).as(ContentTypes.JSON)
  }

  private def loadDocumentation(): InputStream ={
    getClass.getResourceAsStream(swaggerLocation)
  }
}

object SwaggerRoute{
  type YamlObject = JMap[String, Object]

  val swaggerLocation = "/swagger.yaml"
}
