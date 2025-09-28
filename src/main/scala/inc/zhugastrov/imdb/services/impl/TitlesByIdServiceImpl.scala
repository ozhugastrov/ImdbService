package inc.zhugastrov.imdb.services.impl

import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.Title
import inc.zhugastrov.imdb.json.Formats._
import inc.zhugastrov.imdb.services.TitlesByIdService
import inc.zhugastrov.imdb.utils.ClientUtils.{getRequest, parseResponse}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

class TitlesByIdServiceImpl @Inject()(client: Service[Request, Response]) extends TitlesByIdService {
  private val logger = LoggerFactory.getLogger(getClass)

  override def getMoviesById(movieIds: Seq[String]): Future[List[Title]] = {
    val req = getRequest(s"/titles:batchGet", movieIds.map(("titleIds", _)))
    client(req).flatMap(
      rawResponse => {
        parseResponse("TitlesByIdService", rawResponse, {
          logger.debug(s"Got movies response: ${rawResponse.contentString}")
          val json = Json.parse(rawResponse.contentString)
          (json \ "titles").as[List[Title]]
        })
      }
    )
  }
}
