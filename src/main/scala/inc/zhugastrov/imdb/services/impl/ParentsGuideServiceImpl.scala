package inc.zhugastrov.imdb.services.impl

import com.google.inject.Inject
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.ParentsGuide
import inc.zhugastrov.imdb.json.Formats._
import inc.zhugastrov.imdb.services.ParentsGuideService
import inc.zhugastrov.imdb.utils.ClientUtils.{getRequest, parseResponse}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

class ParentsGuideServiceImpl @Inject()(imdbClient: Service[Request, Response]) extends ParentsGuideService {
  private val logger = LoggerFactory.getLogger(getClass)


  override def getParentsGuides(movieId: String): Future[List[ParentsGuide]] = {
    val req = getRequest(s"/titles/$movieId/parentsGuide")
    imdbClient(req).flatMap(
      rawResponse => {
        parseResponse("ParentsGuideService", rawResponse, {
          logger.debug(s"Got guides response: ${rawResponse.contentString}")
          val json = Json.parse(rawResponse.contentString)
          (json \ "parentsGuide").as[List[ParentsGuide]]
        })
      }
    )
  }
}
