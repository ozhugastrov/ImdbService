package inc.zhugastrov.imdb.services.impl

import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.{Await, Future}
import com.typesafe.config.Config
import inc.zhugastrov.imdb.cache.impl.GuavaCacheImpl
import inc.zhugastrov.imdb.domain._
import inc.zhugastrov.imdb.json.Formats._
import inc.zhugastrov.imdb.services.{CastByMovieService, ParentsGuideService, TitleNameToIdService, TitlesByIdService}
import inc.zhugastrov.imdb.utils.ExceptionUtils.getFailedResponse
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}

import java.time.Duration

class ImdbServiceImplTest extends AnyFunSuite with MockitoSugar with BeforeAndAfterEach {
  val titleReads: Reads[Title] = (
    (JsPath \ "id").readWithDefault("0") and
      (JsPath \ "type").read[String] and
      (JsPath \ "primaryTitle").read[String] and
      (JsPath \ "originalTitle").readNullable[String] and
      (JsPath \ "rating").readNullable[Rating] and
      (JsPath \ "startYear").readNullable[Int] and
      (JsPath \ "runtimeInSeconds").readNullable[Int] and
      (JsPath \ "genres").readNullable[List[String]] and
      (JsPath \ "directors").readNullable[List[Person]] and
      (JsPath \ "writers").readNullable[List[Person]]
    )(Title.apply _)

  implicit val movieReads: Reads[Movie] = (
    JsPath.read(titleReads) and
      (JsPath \ "actors").read[List[Actor]] and
      (JsPath \ "cast").read[List[Actor]]
    )((title, actors, cast) => Movie.apply(title, actors ++ cast))

  private val config = mock[Config]
  // Set up config mock for cache.ttl and cache.cleanupInterval
  when(config.getDuration("cache.ttl")).thenReturn(java.time.Duration.ofSeconds(60))
  when(config.getDuration("cache.cleanupInterval")).thenReturn(java.time.Duration.ofSeconds(60))

  private val moviesByIdService = mock[TitlesByIdService]
  private val parentsGuideService = mock[ParentsGuideService]
  private val titleNameToIdService = mock[TitleNameToIdService]
  private val castByMovieService = mock[CastByMovieService]
  private val requestCache = new GuavaCacheImpl[String, Seq[Movie]](Duration.ofSeconds(60))
  private val movieCache = new GuavaCacheImpl[String, Movie](Duration.ofSeconds(60))
  // Provide config to ImdbServiceImpl
  private val imdbService = new ImdbServiceImpl(moviesByIdService, parentsGuideService, titleNameToIdService, castByMovieService, requestCache, movieCache)

  def createRequest(params: Map[String, String]): Request = {
    val request = Request(params = params.toSeq: _*)
    request
  }

  def createMovie(id: String, title: String): Title = Title(
    id = id,
    `type` = "movie",
    primaryTitle = title,
    originalTitle = Some(title),
    rating = None,
    startYear = Some(2020),
    runtimeInSeconds = Some(120),
    genres = Some(List("Action")),
    directors = Some(List()),
    writers = Some(List())
  )

  def createMovieId(id: String): TitleId = TitleId(id)

  private def stubCastByMovie(ids: List[String]): Unit = {
    ids.foreach { id =>
      when(castByMovieService.getCastByMovie(id)).thenReturn(Future.value(List.empty))
    }
  }

  test("successful case") {
    val request = createRequest(Map("titleName" -> "Inception"))
    val movieIds = List(createMovieId("1"), createMovieId("2"))
    val movies = List(createMovie("1", "Inception"), createMovie("2", "Inception 2"))

    when(titleNameToIdService.getMoviesIdByTitle("Inception")).thenReturn(Future.value(movieIds))
    when(moviesByIdService.getMoviesById(List("1", "2"))).thenReturn(Future.value(movies))
    stubCastByMovie(List("1", "2"))

    val response = Await.result(imdbService.apply(request))
    println(response.contentString)
    val returnedMovies = Json.parse(response.contentString).as[List[Movie]]
    assert(response.status == Status.Ok)
    assert(response.contentType.contains("application/json"))
    assert(returnedMovies.length == movies.length)
  }

  test("missed param titleName") {
    val request = createRequest(Map.empty)

    val response = Await.result(imdbService.apply(request))

    assert(response.status == Status.BadRequest)
    assert(response.contentString == "Missing titleName parameter")
  }

  test("wrong param titleName") {
    val request = createRequest(Map("titleNome" -> "Inception"))

    val response = Await.result(imdbService.apply(request))

    assert(response.status == Status.BadRequest)
    assert(response.contentString == "Missing titleName parameter")
  }

  test("get error when getMoviesIdByTitle fails") {
    val request = createRequest(Map("titleName" -> "Inception1"))
    val exception = getFailedResponse("IMDB API error")

    when(titleNameToIdService.getMoviesIdByTitle("Inception1"))
      .thenReturn(exception)

    val response = Await.result(imdbService.apply(request))

    assert(response.status == Status.InternalServerError)
    assert(response.contentString.contains("IMDB API error"))
  }

  test("apply should return InternalServerError for unexpected errors") {
    val request = createRequest(Map("titleName" -> "Inception2"))

    when(titleNameToIdService.getMoviesIdByTitle("Inception2"))
      .thenReturn(Future.value(List(createMovieId("3"))))
    when(moviesByIdService.getMoviesById(List("3")))
      .thenReturn(Future.exception(new RuntimeException("Unexpected error")))

    val response = Await.result(imdbService.apply(request))

    assert(response.status == Status.InternalServerError)
    assert(response.contentString.contains("Unexpected error"))
  }

  test("filters out movies by parentalRatingThreshold") {
    val threshold = 10
    val request = createRequest(Map("titleName" -> "TestTitle", "parentalRatingThreshold" -> threshold.toString))
    val movieIds = List(
      createMovieId("id1"),
      createMovieId("id2"),
      createMovieId("id3"),
      createMovieId("id4"),
      createMovieId("id5")
    )
    val filteredIds = List("id1", "id2", "id3") // These will be filtered out
    val unfilteredIds = List("id4", "id5") // These will remain

    val filterCategory = ParentsGuideCategory.values.head
    val filterSeverity = SeverityLevel.values.head

    val filteredGuide = List(ParentsGuide(
      category = filterCategory,
      severityBreakdowns = List(Severity(filterSeverity, Some(threshold + 1)))
    ))

    val unfilteredGuide = List(ParentsGuide(
      category = filterCategory,
      severityBreakdowns = List(Severity(filterSeverity, Some(threshold - 1)))
    ))

    when(titleNameToIdService.getMoviesIdByTitle("TestTitle")).thenReturn(Future.value(movieIds))
    filteredIds.foreach { id =>
      when(parentsGuideService.getParentsGuides(id)).thenReturn(Future.value(filteredGuide))
    }
    unfilteredIds.foreach { id =>
      when(parentsGuideService.getParentsGuides(id)).thenReturn(Future.value(unfilteredGuide))
    }
    stubCastByMovie(movieIds.map(_.id))

    val movies = unfilteredIds.map(id => createMovie(id, s"Movie $id"))
    when(moviesByIdService.getMoviesById(movieIds.map(_.id))).thenReturn(Future.value(movies))

    val response = Await.result(imdbService.apply(request))
    assert(response.status == Status.Ok)
    val returnedMovies = Json.parse(response.contentString).as[List[Movie]]
    assert(returnedMovies.map(_.title.id).length == unfilteredIds.length)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(moviesByIdService, parentsGuideService, titleNameToIdService, castByMovieService)
  }
}
