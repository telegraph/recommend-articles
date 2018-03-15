package uk.co.telegraph.recommend.articles

import io.restassured.module.scala.RestAssuredSupport._

class DocumentationCheckSpecs extends ComponentTest
{

  info("")
  info("")
  info("As a User")
  info("I want to be able to use the Documentation Endpoint")
  info("So I can get both Json/Yaml versions of the documentation")
  info("")

  feature("Documentation endpoint") {
    scenario("Yaml format", ctAndIt: _*) {
      serviceComponent.whenCallDocumentation(format = "yaml")
      .Then()
        .statusCode(200)
        .header("Content-Type", "text/yaml")
    }

    scenario("Json format", ctAndIt: _*) {
      serviceComponent.whenCallDocumentation(format = "json")
      .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
    }
  }
}
