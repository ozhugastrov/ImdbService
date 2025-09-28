package inc.zhugastrov.imdb.domain

object ParentsGuideCategory extends Enumeration {
  type ParentsGuideCategory = Value
  val SEXUAL_CONTENT, VIOLENCE, PROFANITY, ALCOHOL_DRUGS, FRIGHTENING_INTENSE_SCENES = Value
  val filterBy = Set(SEXUAL_CONTENT, VIOLENCE)
}
