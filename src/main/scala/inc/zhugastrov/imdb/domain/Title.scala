package inc.zhugastrov.imdb.domain

case class Title(
                  id: String,
                  `type`: String,
                  primaryTitle: String,
                  originalTitle: Option[String],
                  rating: Option[Rating],
                  startYear: Option[Int],
                  runtimeInSeconds: Option[Int],
                  genres: Option[List[String]],
                  directors: Option[List[Person]],
                  writers: Option[List[Person]],
                )
case class TitleId(
                    id: String
                  )