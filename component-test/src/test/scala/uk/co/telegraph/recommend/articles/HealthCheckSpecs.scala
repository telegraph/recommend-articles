package uk.co.telegraph.recommend.articles

import io.restassured.module.scala.RestAssuredSupport._
import org.hamcrest.Matchers.{equalTo, hasItems}

class HealthCheckSpecs extends ComponentTest
{

  info("")
  info("")
  info("As a User")
  info("I want to be able to use the health endpoint")
  info("So I can get a full health check")
  info("And collect information about the clients")
  info("")

  feature("Health check endpoint") {
    scenario("the health check should be healthy", ctAndIt:_*) {
      serviceComponent.whenCallHealthEndpoint()
      .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body("status",       equalTo  ("Healthy"))
        .body("name",         equalTo  (serviceComponent.name))
        .body("cached",       equalTo  (false))
        .body("clients.name", hasItems (storageComponent.name, recommenderComponent.name))
    }

    scenario(s"the health check should be unhealthy if a client is unreachable", ctTag) {
      storageComponent.setOffline()
      serviceComponent.whenCallHealthEndpoint()
      .Then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body("status",       equalTo("Unhealthy"))
        .body("name",         equalTo  (serviceComponent.name))
        .body("cached",       equalTo  (false))
        .body("clients.name", hasItems (storageComponent.name, recommenderComponent.name))
    }
  }
}

