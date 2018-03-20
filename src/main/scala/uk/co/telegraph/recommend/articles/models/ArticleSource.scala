package uk.co.telegraph.recommend.articles.models

sealed trait ArticleSource

object ArticleSource {
  case object All                    extends ArticleSource
  case class  Only(only:Seq[String]) extends ArticleSource

  def apply(sources:Set[String]): ArticleSource = {
    sources.map(_.toLowerCase).toList match {
      case "all" :: _ | Nil => All
      case only             => Only(only)
    }
  }
}
