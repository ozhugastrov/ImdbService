package inc.zhugastrov.imdb.services.impl

import com.google.inject.Inject
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Future, Return, Throw}
import com.typesafe.config.Config
import inc.zhugastrov.imdb.domain.Movie
import inc.zhugastrov.imdb.domain.ParentsGuideCategory.{filterBy => categoryFilter}
import inc.zhugastrov.imdb.domain.SeverityLevel.{filterBy => severityFilter}
import inc.zhugastrov.imdb.json.Formats._
import inc.zhugastrov.imdb.services._
import inc.zhugastrov.imdb.utils.{Cache, ExternalServiceException}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json


class ImdbServiceImpl @Inject()(titlesByIdService: TitlesByIdService,
                                parentsGuideService: ParentsGuideService,
                                titleNameToIdService: TitleNameToIdService,
                                castByMovieService: CastByMovieService,
                                config: Config) extends ImdbService {
  private val ttl = config.getDuration("cache.ttl")
  private val cleanupInterval = config.getDuration("cache.cleanupInterval")
  private val requestCache = new Cache[String, Seq[Movie]](ttl, cleanupInterval)
  private val movieCache = new Cache[String, Movie](ttl, cleanupInterval)
  private val logger = LoggerFactory.getLogger(getClass)

  override def apply(request: Request): Future[Response] = {
    val titleNameOps = request.params.get("titleName")
    titleNameOps.map(titleName => {
      val parentalThreshold = request.params.getInt("parentalRatingThreshold")
      logger.debug(s"Received request for titleName: $titleName with parentalRatingThreshold: $parentalThreshold")
      val requestCacheHit = requestCache.get(titleName).flatMap(res =>
        parentalThreshold.fold {
          logger.debug(s"Returning from cache for titleName: $titleName")
          Option(Future.value(res))
        }(_ => None))
      val moviesF = requestCacheHit.getOrElse {
        val titleIds = titleNameToIdService.getMoviesIdByTitle(titleName)
        val filteredTitlesF = titleIds.flatMap(titleId => parentalThreshold.map(threshold => {
          Future.collect(titleId.map(title =>
            parentsGuideService.getParentsGuides(title.id)
              .map(pgs => (title, pgs))
          )).map(_.filterNot { case (_, guides) => guides.exists(guide => categoryFilter.contains(guide.category) &&
            guide.severityBreakdowns.exists(severity => severityFilter.contains(severity.severityLevel) &&
              severity.voteCount.exists(_ >= threshold)))
          }.map(_._1))
        }).getOrElse(Future.value(titleId)))

        filteredTitlesF.flatMap(mid => {
          val (cached, ids) = mid.partitionMap(movId => movieCache.get(movId.id).map(Left(_)).getOrElse(Right(movId)))
          Future.collect(ids.map(_.id).grouped(5).toSeq.map(titlesByIdService.getMoviesById))
            .map(_.flatten)
            .flatMap(titles => Future.collect(titles.map(title => {
              castByMovieService.getCastByMovie(title.id).map(Movie(title, _))
            }
            )))
            .map(_.map(mov => movieCache.put(mov.title.id, mov)))
            .map(_.concat(cached))
        }).map(movs => requestCache.put(titleName, movs))
      }
      moviesF.transform {
        case Return(movies) =>
          val response = Response(Status.Ok)
          logger.debug(s"Returning movies: ${movies.mkString("\n")}")
          response.contentString = Json.toJson(movies).toString()
          response.contentType = "application/json"
          Future.value(response)
        case Throw(ex: ExternalServiceException) =>
          Future.value(ex.response)
        case Throw(ex) =>
          val response = Response(Status.InternalServerError)
          logger.error(s"Unexpected error processing request for titleName: $titleName", ex)
          response.contentString = s"Unexpected error: ${ex.getMessage}"
          Future.value(response)
      }

    }).getOrElse(
      {
        logger.debug("Missing titleName parameter in request")
        val response = Response(Status.BadRequest)
        response.contentString = "Missing titleName parameter"
        Future.value(response)
      })
  }
}