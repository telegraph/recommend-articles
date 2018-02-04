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
  val sampleLocalDateTime  : LocalDateTime = LocalDateTime.ofEpochSecond(1483228800, 0, ZoneOffset.UTC)
  val sampleZoneDateTime   : ZonedDateTime = ZonedDateTime.of(sampleLocalDateTime, ZoneId.of("UTC"))
  val sampleZoneDateTimeStr: String        = sampleZoneDateTime.format(zonedDateTimeFormatter)
  val sampleContentId1     : String        = "11111111-1111-1111-1111-111111111111"
  val sampleContentId2     : String        = "22222222-2222-2222-2222-222222222222"

  val sampleContentObj1 = UnifiedContentModel(
    metadata = Metadata(
      `content-id`            = sampleContentId1,
      `type`                  = ContentModelEnum.Article,
      premium                 = false,
      language                = "en-GB",
      `tmg-created-date`      = sampleZoneDateTime,
      `tmg-last-modified-date`= sampleZoneDateTime,
      source                  = Some(MetadataSource(
        `original-source`    = "Test",
        `original-feed-name` = "Fake",
        `source-id`          = "1",
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
          key   = "uri",
          value = "http://www.telegraph.co.uk/test/fake/article1"
        )
      )
    ),
    content = Content(
      headline = "Fake, \"Headline's\"",
      byline = Some("By Fake Author"),
      authors = Seq(Author(
        name = "Fake Author",
        uri  = Some("wwww.telegraph.co.uk/fake/author"),
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
          data        = Some("http://www.telegraph.co.uk/first/image.jpeg"),
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
      language                = "en-GB",
      `tmg-created-date`      = sampleZoneDateTime,
      `tmg-last-modified-date`= sampleZoneDateTime,
      source                  = Some(MetadataSource(
        `original-source`    = "Test",
        `original-feed-name` = "Fake",
        `source-id`          = "2",
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
          key   = "uri",
          value = "http://www.telegraph.co.uk/test/fake/article2"
        )
      )
    ),
    content = Content(
      headline = "Fake Headline",
      byline = Some("By Fake Author"),
      authors = Seq(Author(
        name = "Fake Author",
        uri  = Some("wwww.telegraph.co.uk/fake/author"),
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
          data        = Some("http://www.telegraph.co.uk/first/image.jpeg"),
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

  val sampleRecommenderRequest = RecommenderRequest(
    channel = "News",
    headline= "Fake Article",
    body    = "fake article body"
  )
  val sampleRecommenderResponse = RecommenderResponse(
    `result-count`= 2,
    data          = Seq(
      RecommenderItem( sampleContentId1, 0.9),
      RecommenderItem( sampleContentId2, 0.8)
    )
  )

  val sampleRecommendArticleRequest = RecommendArticleRequest(
    sort = Sort( field = "score", order = SortOrderEnum.asc),
    limit          = 10,
    offset         = 0,
    `query-filters`= RecommendArticleQueryFilter(
      source     = Set(),
      `date-from`= None,
      `date-to`  = None,
      channel    = None
    ),
    `query-object` = RecommendArticleQueryObject(
      channel  = "News",
      headline = "Fake Article",
      body     = "Fake Article body"
    )
  )
  val sampleRecommendArticleResult = RecommendArticleResult( Seq(
    RecommendArticleItem(
      id       = sampleContentId1,
      score    = 0.9,
      `type`   = "article",
      headline = "Fake, \"Headline's\"",
      url      = None,
      thumbnail= None,
      pubdate  = Some(sampleZoneDateTimeStr),
      channel  = None,
      source   = "1",
      authors  = Seq("Fake Author")
    ),
    RecommendArticleItem(
      id       = sampleContentId2,
      score    = 0.8,
      `type`   = "article",
      headline = "Fake Headline",
      url      = None,
      thumbnail= None,
      pubdate  = Some(sampleZoneDateTimeStr),
      channel  = None,
      source   = "2",
      authors  = Seq("Fake Author")
    )
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
    HttpEntity(`application/json`, s"""{"channel":"News","headline":"Fake Article","body":"fake article body"}""")
  )
  val sampleRecommendationResponse = HttpResponse(
    status = OK,
    entity = HttpEntity(`application/json`, """{"result-count":2,"data":[{"content-id":"11111111-1111-1111-1111-111111111111","score":0.9}, {"content-id":"22222222-2222-2222-2222-222222222222","score":0.8}]}""")
  )
  val sampleInvalidResponse = HttpResponse(status = InternalServerError)

}

object TestData extends TestData
