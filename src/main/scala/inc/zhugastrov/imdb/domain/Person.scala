package inc.zhugastrov.imdb.domain

case class Person(
              id: String,
              displayName: String,
              alternativeNames: Option[List[String]]
            )
