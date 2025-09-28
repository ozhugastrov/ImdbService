package inc.zhugastrov.imdb.domain

import inc.zhugastrov.imdb.domain.SeverityLevel.SeverityLevel

case class Severity(severityLevel: SeverityLevel, voteCount: Option[Int])
