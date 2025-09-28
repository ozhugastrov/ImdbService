package inc.zhugastrov.imdb.json


import inc.zhugastrov.imdb.domain.Movie.actorCategories
import inc.zhugastrov.imdb.domain.ParentsGuideCategory.ParentsGuideCategory
import inc.zhugastrov.imdb.domain.SeverityLevel.SeverityLevel
import inc.zhugastrov.imdb.domain._
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

object Formats {
  private def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = Format(
    Reads {
      case JsString(s) => JsSuccess(enum.withName(s))
      case _ => JsError("String value expected")
    },
    Writes(severityLevel => JsString(severityLevel.toString))
  )

  implicit val severityLevelFormat: Format[SeverityLevel] = enumFormat(SeverityLevel)
  implicit val parentalGuideCategoryFormat: Format[ParentsGuideCategory] = enumFormat(ParentsGuideCategory)

  implicit val ratingFormat: Format[Rating] = Json.format[Rating]
  implicit val writerFormat: Format[Person] = Json.format[Person]
  implicit val actorFormat: Format[Actor] = {
    val actorReads: Reads[Actor] = (
      (JsPath \ "name").read[Person] and
        (JsPath \ "category").read[String]
      )(Actor.apply _)
    val actorWrites: Writes[Actor] = (
      JsPath.write[Person] and
        (JsPath \ "category").write[String]
      )(unlift(Actor.unapply))
    Format(actorReads, actorWrites)
  }
  implicit val titleFormat: Format[Title] = {
    val titleReads = Json.reads[Title]
    val titleWrites: Writes[Title] = (
      (JsPath \ "type").write[String] and
        (JsPath \ "primaryTitle").write[String] and
        (JsPath \ "originalTitle").writeNullable[String] and
        (JsPath \ "genres").writeNullable[List[String]] and
        (JsPath \ "rating").writeNullable[Rating] and
        (JsPath \ "startYear").writeNullable[Int] and
        (JsPath \ "runtimeInSeconds").writeNullable[Int] and
        (JsPath \ "directors").writeNullable[List[Person]] and
        (JsPath \ "writers").writeNullable[List[Person]]
      )(title => (title.`type`, title.primaryTitle, title.originalTitle,
      title.genres, title.rating, title.startYear,
      title.runtimeInSeconds, title.directors.map(_.take(5)), title.writers.map(_.take(5))))

    Format(titleReads, titleWrites)
  }
  implicit val titleIdFormat: Format[TitleId] = Json.format[TitleId]
  implicit val severityFormat: Format[Severity] = Json.format[Severity]
  implicit val parentsGuideFormat: Format[ParentsGuide] = Json.format[ParentsGuide]

  implicit val movieWrites: Writes[Movie] = (
    JsPath.write[Title] and
      (JsPath \ "actors").write[List[Actor]] and
      (JsPath \ "cast").write[List[Actor]]
    )(movie => {
    val actors = movie.credits.filter(actor => actorCategories.contains(actor.category)).take(5)
    val cast = movie.credits.filter(actor => !actors.map(_.person.id).contains(actor.person.id)).take(5)
    (movie.title, actors, cast)
  })
}
