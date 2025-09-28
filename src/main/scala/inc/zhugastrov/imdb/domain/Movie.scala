package inc.zhugastrov.imdb.domain

case class Movie(
                  title: Title,
                  credits: List[Actor]
                )

object Movie {
  val actorCategories = Set("actor", "actress")
}
