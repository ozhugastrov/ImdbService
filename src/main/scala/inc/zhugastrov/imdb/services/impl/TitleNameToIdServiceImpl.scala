package inc.zhugastrov.imdb.services.impl

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.TitleId
import inc.zhugastrov.imdb.json.Formats._
import inc.zhugastrov.imdb.services.TitleNameToIdService
import inc.zhugastrov.imdb.utils.ClientUtils.{getRequest, parseResponse}
import jakarta.inject.Inject
import org.slf4j.LoggerFactory
import play.api.libs.json._


class TitleNameToIdServiceImpl @Inject()(imdbClient: Service[Request, Response]) extends TitleNameToIdService {
  private val logger = LoggerFactory.getLogger(getClass)

  override def getMoviesIdByTitle(titleName: String): Future[List[TitleId]] = {
    val req = getRequest("/search/titles", Seq(("query", titleName)))
    imdbClient(req).flatMap { rawResponse =>
      parseResponse("TitleNameToIdService", rawResponse, {
        logger.debug(s"Got titles response: ${rawResponse.contentString}")
        val json = Json.parse(rawResponse.contentString)
        (json \ "titles").as[List[TitleId]]
      })
    }
  }
}
