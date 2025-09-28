package inc.zhugastrov.imdb.domain

import inc.zhugastrov.imdb.domain.ParentsGuideCategory.ParentsGuideCategory

case class ParentsGuide(category: ParentsGuideCategory, severityBreakdowns: List[Severity])
