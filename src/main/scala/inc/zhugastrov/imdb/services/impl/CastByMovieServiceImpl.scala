package inc.zhugastrov.imdb.services.impl

import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.Actor
import inc.zhugastrov.imdb.json.Formats._
import inc.zhugastrov.imdb.services.CastByMovieService
import inc.zhugastrov.imdb.utils.ClientUtils.{getRequest, parseResponse}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

class CastByMovieServiceImpl @Inject()(imdbClient: Service[Request, Response]) extends CastByMovieService {
  private val logger = LoggerFactory.getLogger(getClass)


  override def getCastByMovie(movieId: String): Future[List[Actor]] = {
    val req = getRequest(s"/titles/$movieId/credits", Seq(("pageSize", "50")))
    imdbClient(req).flatMap(
      rawResponse => {
        parseResponse("CastByMovieService", rawResponse, {
          logger.debug(s"Got credits response: ${rawResponse.contentString}")
          val json = Json.parse(rawResponse.contentString)
          (json \ "credits").asOpt[List[Actor]].getOrElse(List.empty)
        })
      }
    )
  }
}