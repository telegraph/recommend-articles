package uk.co.telegraph.recommend.articles

import com.typesafe.config.{Config, ConfigFactory}

package object environment {

  lazy val environment      : String = System.getenv().getOrDefault("ENVIRONMENT", "ct")
  lazy val componentTestMode: Boolean = environment == "ct"
  lazy val environmentConfig: Config =  ConfigFactory.load(s"application.$environment.conf")

}
