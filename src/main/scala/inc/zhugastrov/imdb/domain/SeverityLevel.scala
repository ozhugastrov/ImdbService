package inc.zhugastrov.imdb.domain

object SeverityLevel extends Enumeration {
  type SeverityLevel = Value
  val none, mild, moderate, severe = Value
  val filterBy = Set(moderate, severe)
}
