package inc.zhugastrov.imdb.services

import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.{Title, TitleId}

trait TitleNameToIdService {
  def getMoviesIdByTitle(titleName: String): Future[List[TitleId]]
}