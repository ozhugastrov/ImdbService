package inc.zhugastrov.imdb.services

import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.Title

trait TitlesByIdService {
  def getMoviesById(movieIds: Seq[String]): Future[List[Title]]
}

