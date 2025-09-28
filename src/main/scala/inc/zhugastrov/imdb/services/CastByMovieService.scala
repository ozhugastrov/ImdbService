package inc.zhugastrov.imdb.services

import com.twitter.util.Future
import inc.zhugastrov.imdb.domain.Actor

trait CastByMovieService {
  def getCastByMovie(movieId: String): Future[List[Actor]]

}
