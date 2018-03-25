package uk.co.telegraph.recommend.articles.models

sealed trait ArticleChannel

object ArticleChannel {
  case object All                    extends ArticleChannel
  case class  Only(only:Seq[String]) extends ArticleChannel

  def apply(sources:Set[String]): ArticleChannel = {
    sources.map(_.toLowerCase).toList match {
      case "all" :: _ | Nil => All
      case only             => Only(only.map(_.toLowerCase))
    }
  }
}
