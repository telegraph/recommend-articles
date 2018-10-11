package uk.co.telegraph.recommend.articles

import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import com.typesafe.config.{Config, ConfigFactory}
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Formats}
import uk.co.telegraph.recommend.articles.clients.recommender.RecommenderClient
import uk.co.telegraph.recommend.articles.clients.recommender.model.{RecommenderItem, RecommenderRequest, RecommenderResponse}
import uk.co.telegraph.recommend.articles.clients.storage.StorageClient
import uk.co.telegraph.recommend.articles.models._
import uk.co.telegraph.recommend.articles.routes.models._
import uk.co.telegraph.recommend.articles.utils._
import uk.co.telegraph.ucm.domain.enrich.UnifiedEnrichContentModel
import uk.co.telegraph.ucm.domain.{Author, BodyImage, BodyText, Content, ContentModelEnum, Metadata, MetadataExtension, MetadataNameUri, MetadataRights, MetadataSource, UnifiedContentModel}

import scala.io.Source.fromInputStream

trait TestData {

  implicit val formats:Formats = DefaultFormats ++
    UnifiedContentModel.serializers ++
    UnifiedEnrichContentModel.serializers

  val testDefaultConfig    : Config        = ConfigFactory.load("application.tst.conf")
  val sampleLocalDateTime  : LocalDateTime = LocalDateTime.ofEpochSecond(1483272000, 0, ZoneOffset.UTC)
  val sampleZoneDateTime   : ZonedDateTime = ZonedDateTime.of(sampleLocalDateTime, ZoneId.of("UTC"))
  val sampleZoneDateTimeStr: String        = sampleZoneDateTime
  val sampleContentId1     : String        = "11111111-1111-1111-1111-111111111111"
  val sampleContentId2     : String        = "22222222-2222-2222-2222-222222222222"
  val sampleContentId3     : String        = "33333333-3333-3333-3333-333333333333"

  val sampleContentObj1 = UnifiedContentModel(
    metadata = Metadata(
      `content-id`            = sampleContentId1,
      `type`                  = ContentModelEnum.Article,
      premium                 = false,
      active                  = Some(true),
      language                = "en-GB",
      `tmg-created-date`      = sampleZoneDateTime,
      `tmg-last-modified-date`= sampleZoneDateTime,
      source                  = Some(MetadataSource(
        `original-source`    = "Telegraph",
        `original-feed-name` = "AEM",
        `source-id`          = "tmg",
        `created-date`       = Some(sampleZoneDateTime)
      )),
      desk                    = Some("Fake Article"),
      rights                  = Seq(
        MetadataRights(
          `copyright-holder`      = Some("copyright-holder"),
          `copyright-notice`      = Some("copyright-notice"),
          `use-allowed-from-date` = Some("use-allowed-from-date"),
          `use-allowed-to-date`   = Some("use-allowed-to-date")
        )
      ),
      location                = Seq(
        MetadataNameUri(
          name = "london",
          uri = Some("www.telegraph.co.uk/london")
        )
      ),
      annotations             = Seq(
        MetadataNameUri(
          name = "article",
          uri = Some("www.telegraph.co.uk/article")
        )
      ),
      extensions              = Seq(
        MetadataExtension(
          key   = "channel",
          value = "news"
        ),
        MetadataExtension(
          key   = "url",
          value = "http://www.telegraph.co.uk/news/article-1/"
        )
      )
    ),
    content = Content(
      headline = "Fake, \"Headline's\"",
      byline = Some("By Fake Author"),
      authors = Seq(Author(
        name = "Fake Author",
        uri  = Some("wwww.telegraph.co.uk/fake/author"),
        url  = Some("wwww.telegraph.co.uk/fake/url"),
        role = Some("Fake Journalist"),
        location = Some("somewhere")
      )),
      body = Seq(
        BodyText(
          data        = Some("first paragraph"),
          `alt-text`  = None,
          `html-data` = Some("<p>first paragraph</p>")
        ),
        BodyImage(
          data        = Some("http://www.telegraph.co.uk/first/image1.jpeg"),
          `alt-text`  = Some("first-image-alt-text"),
          credit      = Some(""),
          caption     = Some("First Image"),
          `html-caption` = Some("<p>first paragraph</p>")
        )
      ),
      gallery    = Seq.empty,
      reviews    = Seq.empty,
      livestream = Seq.empty
    )
  )
  val sampleContentObj2 = UnifiedContentModel(
    metadata = Metadata(
      `content-id`            = sampleContentId2,
      `type`                  = ContentModelEnum.Article,
      premium                 = false,
      active                  = Some(true),
      language                = "en-GB",
      `tmg-created-date`      = "2016-01-01T12:00:00.000Z",
      `tmg-last-modified-date`= "2016-01-01T12:00:00.000Z",
      source                  = Some(MetadataSource(
        `original-source`    = "PA",
        `original-feed-name` = "PA",
        `source-id`          = "pa",
        `created-date`       = Some("2016-01-01T12:00:00.000Z")
      )),
      desk                    = Some("Fake Article"),
      rights                  = Seq(
        MetadataRights(
          `copyright-holder`      = Some("copyright-holder"),
          `copyright-notice`      = Some("copyright-notice"),
          `use-allowed-from-date` = Some("use-allowed-from-date"),
          `use-allowed-to-date`   = Some("use-allowed-to-date")
        )
      ),
      location                = Seq(
        MetadataNameUri(
          name = "london",
          uri = Some("www.telegraph.co.uk/london")
        )
      ),
      annotations             = Seq(
        MetadataNameUri(
          name = "article",
          uri = Some("www.telegraph.co.uk/article")
        )
      ),
      extensions              = Seq(
        MetadataExtension(
          key   = "channel",
          value = "sports"
        ),
        MetadataExtension(
          key   = "url",
          value = "http://www.telegraph.co.uk/news/article-2/"
        )
      )
    ),
    content = Content(
      headline = "Fake Headline",
      byline = Some("By Fake Author"),
      authors = Seq(Author(
        name = "Fake Author",
        uri  = Some("wwww.telegraph.co.uk/fake/author"),
        url  = Some("wwww.telegraph.co.uk/fake/author/url"),
        role = Some("Fake Journalist"),
        location = Some("somewhere")
      )),
      body = Seq(
        BodyText(
          data        = Some("first paragraph"),
          `alt-text`  = None,
          `html-data` = Some("<p>first paragraph</p>")
        ),
        BodyImage(
          data        = Some("http://www.telegraph.co.uk/first/image2.jpeg"),
          `alt-text`  = Some("first-image-alt-text"),
          credit      = Some(""),
          caption     = Some("First Image"),
          `html-caption` = Some("<p>first paragraph</p>")
        )
      ),
      gallery    = Seq.empty,
      reviews    = Seq.empty,
      livestream = Seq.empty
    )
  )
  val sampleContentObj3 = UnifiedContentModel(
    metadata = Metadata(
      `content-id`            = sampleContentId3,
      `type`                  = ContentModelEnum.Article,
      premium                 = false,
      active                  = Some(false),
      language                = "en-GB",
      `tmg-created-date`      = "2016-01-01T12:00:00.000Z",
      `tmg-last-modified-date`= "2016-01-01T12:00:00.000Z",
      source                  = Some(MetadataSource(
        `original-source`    = "PA",
        `original-feed-name` = "PA",
        `source-id`          = "pa",
        `created-date`       = Some("2016-01-01T12:00:00.000Z")
      )),
      desk                    = Some("Fake Article"),
      rights                  = Seq(
        MetadataRights(
          `copyright-holder`      = Some("copyright-holder"),
          `copyright-notice`      = Some("copyright-notice"),
          `use-allowed-from-date` = Some("use-allowed-from-date"),
          `use-allowed-to-date`   = Some("use-allowed-to-date")
        )
      ),
      location                = Seq(
        MetadataNameUri(
          name = "london",
          uri = Some("www.telegraph.co.uk/london")
        )
      ),
      annotations             = Seq(
        MetadataNameUri(
          name = "article",
          uri = Some("www.telegraph.co.uk/article")
        )
      ),
      extensions              = Seq(
        MetadataExtension(
          key   = "channel",
          value = "sports"
        ),
        MetadataExtension(
          key   = "url",
          value = "http://www.telegraph.co.uk/news/article-2/"
        )
      )
    ),
    content = Content(
      headline = "Fake Headline",
      byline = Some("By Fake Author"),
      authors = Seq(Author(
        name = "Fake Author",
        uri  = Some("wwww.telegraph.co.uk/fake/author"),
        url  = Some("wwww.telegraph.co.uk/fake/author/url"),
        role = Some("Fake Journalist"),
        location = Some("somewhere")
      )),
      body = Seq(
        BodyText(
          data        = Some("first paragraph"),
          `alt-text`  = None,
          `html-data` = Some("<p>first paragraph</p>")
        ),
        BodyImage(
          data        = Some("http://www.telegraph.co.uk/first/image2.jpeg"),
          `alt-text`  = Some("first-image-alt-text"),
          credit      = Some(""),
          caption     = Some("First Image"),
          `html-caption` = Some("<p>first paragraph</p>")
        )
      ),
      gallery    = Seq.empty,
      reviews    = Seq.empty,
      livestream = Seq.empty
    )
  )

  val sampleContentIds   = Set(sampleContentId1,  sampleContentId2)
  val sampleContentItems = Seq(sampleContentObj1, sampleContentObj2)
  val sampleContentIds__containingOneInactiveArticle  = Set(sampleContentId1,  sampleContentId2, sampleContentId3)

  val sampleRecommenderRequest = RecommenderRequest(
    channel = Some("News"),
    headline= Some("Fake Article"),
    body    = "Fake Article body - This body is for test purposes only and should not be used in production"
  )
  val sampleRecommenderResponse = RecommenderResponse(
    `result-count`= 2,
    data           = Seq(
      RecommenderItem( sampleContentId1, 0.9, "2017-01-01T12:00:00.000Z"),
      RecommenderItem( sampleContentId2, 0.8, "2016-01-01T12:00:00.000Z")
    )
  )

  val sampleRecommenderResponse_containingOneInactiveArticle = RecommenderResponse(
    `result-count`= 3,
    data           = Seq(
      RecommenderItem( sampleContentId1, 0.9, "2017-01-01T12:00:00.000Z"),
      RecommenderItem( sampleContentId2, 0.8, "2016-01-01T12:00:00.000Z"),
      RecommenderItem( sampleContentId3, 0.7, "2015-01-01T12:00:00.000Z")
    )
  )

  val sampleRecommendArticle = RecommendArticles(
    sort           = Sort("score", SortOrderEnum.asc),
    limit          = 10,
    offset         = 0,
    queryFilter    = QueryFilter(
      source       = ArticleSource.All,
      dateFrom     = None,
      dateTo       = None,
      channel      = ArticleChannel.All
    ),
    queryObject    = QueryObject(
      channel      = Some("News"),
      headline     = Some("Fake Article"),
      body         = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )

  val sampleRecommendArticleWithDateRange = RecommendArticles(
    sort           = Sort("score", SortOrderEnum.asc),
    limit          = 10,
    offset         = 0,
    queryFilter    = QueryFilter(
      source       = ArticleSource.All,
      dateFrom     = Some("2017-01-01T11:00:00.000Z"),
      dateTo       = Some("2017-01-01T13:00:00.000Z"),
      channel      = ArticleChannel.All
    ),
    queryObject    = QueryObject(
      channel      = Some("News"),
      headline     = Some("Fake Article"),
      body         = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )

  val sampleRecommendArticleWithOffsetAndLimit = RecommendArticles(
    sort           = Sort("score", SortOrderEnum.asc),
    limit          = 1,
    offset         = 1,
    queryFilter    = QueryFilter(
      source       = ArticleSource.All,
      dateFrom     = None,
      dateTo       = None,
      channel      = ArticleChannel.All
    ),
    queryObject    = QueryObject(
      channel      = Some("News"),
      headline     = Some("Fake Article"),
      body         = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )

  val sampleRecommendArticleWithSource = RecommendArticles(
    sort           = Sort("score", SortOrderEnum.asc),
    limit          = 10,
    offset         = 0,
    queryFilter    = QueryFilter(
      source       = ArticleSource.Only(Seq("aem")),
      dateFrom     = None,
      dateTo       = None,
      channel      = ArticleChannel.All
    ),
    queryObject    = QueryObject(
      channel      = Some("News"),
      headline     = Some("Fake Article"),
      body         = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )

  val sampleRecommendArticleWithChannel = RecommendArticles(
    sort           = Sort("score", SortOrderEnum.asc),
    limit          = 10,
    offset         = 0,
    queryFilter    = QueryFilter(
      source       = ArticleSource.All,
      dateFrom     = None,
      dateTo       = None,
      channel      = ArticleChannel.Only(Seq("news"))
    ),
    queryObject    = QueryObject(
      channel      = Some("News"),
      headline     = Some("Fake Article"),
      body         = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )

  val sampleRecommendArticleRequest = RecommendArticlesRequest(
    sort           = "+score",
    limit          = 10,
    offset         = 0,
    `query-filters`= RecommendArticlesQueryFilter(
      source       = Set(),
      `date-from`  = None,
      `date-to`    = None,
      channel      = Set()
    ),
    `query-object` = RecommendArticlesQueryObject(
      channel      = Some("News"),
      headline     = Some("Fake Article"),
      body         = "Fake Article body - This body is for test purposes only and should not be used in production"
    )
  )
  val sampleRecommendArticle_1 = RecommendArticleItem(
    id       = sampleContentId1,
    score    = 0.9,
    `type`   = "article",
    headline = "Fake, \"Headline's\"",
    url      = Some("http://www.telegraph.co.uk/news/article-1/"),
    thumbnail= Some("http://www.telegraph.co.uk/first/image1.jpeg"),
    pubdate  = Some(sampleZoneDateTimeStr),
    channel  = Some("news"),
    source   = "AEM",
    authors  = Seq("Fake Author")
  )
  val sampleRecommendArticle_2 = RecommendArticleItem(
    id       = sampleContentId2,
    score    = 0.8,
    `type`   = "article",
    headline = "Fake Headline",
    url      = Some("http://www.telegraph.co.uk/news/article-2/"),
    thumbnail= Some("http://www.telegraph.co.uk/first/image2.jpeg"),
    pubdate  = Some("2016-01-01T12:00:00.000Z"),
    channel  = Some("sports"),
    source   = "PA",
    authors  = Seq("Fake Author")
  )

  val sampleRecommendArticleResult = RecommendArticleResult(Seq(
    sampleRecommendArticle_1,
    sampleRecommendArticle_2
  ))

  val sampleException  = new RuntimeException("Fake Exception")

  def fromPayload(name:String):String = {
    fromInputStream( getClass.getResourceAsStream( name ) )
      .getLines
      .mkString("\n")
  }

  def fromCompactPayload(name:String):String = {
    JsonMethods.compact(JsonMethods.parse( fromPayload(name) ))
  }

  def fromPayloadAsJValue(name: String): JValue = {
    JsonMethods.parse( fromPayload(name) )
  }
}

trait TestDataStorageClient extends TestData {

  val config : Config = testDefaultConfig.getConfig(StorageClient.defaultConfigPath)
  val baseUrl: String = config.getString("baseUrl")

  val sampleGetByIdRequestContent1 = RequestBuilding.Get(s"$baseUrl/content/$sampleContentId1")
  val sampleGetByIdRequestContent2 = RequestBuilding.Get(s"$baseUrl/content/$sampleContentId2")

  val sampleGetByIdResponseContent1 = HttpResponse(
    status = OK,
    entity = HttpEntity(`application/json`, fromCompactPayload("/storage-model/payload-content1.json"))
  )
  val sampleGetByIdResponseContent2 = HttpResponse(
    status = OK,
    entity = HttpEntity(`application/json`, fromCompactPayload("/storage-model/payload-content2.json"))
  )

  val sampleNotFoundResponse = HttpResponse(status = NotFound)
}

trait TestDataRecommenderClient extends TestData {

  val config : Config = testDefaultConfig.getConfig(RecommenderClient.defaultConfigPath)
  val baseUrl: String = config.getString("baseUrl")

  val sampleGetRecommendationsForRequest = RequestBuilding.Post(
    s"$baseUrl/article",
    HttpEntity(`application/json`, s"""{"fields":["content-id","score","weight","date-last-modified"],"channel":"News","headline":"Fake Article","body":"Fake Article body - This body is for test purposes only and should not be used in production"}""")
  )
  val sampleRecommendationResponse = HttpResponse(
    status = OK,
    entity = HttpEntity(`application/json`, """{"result-count":2,"data":[{"content-id":"11111111-1111-1111-1111-111111111111","score":0.9,"date-last-modified":"2017-01-01T12:00:00.000Z"}, {"content-id":"22222222-2222-2222-2222-222222222222","score":0.8,"date-last-modified":"2016-01-01T12:00:00.000Z"}]}""")
  )
  val sampleInvalidResponse = HttpResponse(status = InternalServerError)
}

object TestData extends TestData
