package inc.zhugastrov.imdb.services

import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.ParentsGuide

trait ParentsGuideService {
  def getParentsGuides(movieId: String): Future[List[ParentsGuide]]
}