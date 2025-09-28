package inc.zhugastrov.imdb

import com.google.inject.AbstractModule
import com.typesafe.config.Config
import inc.zhugastrov.imdb.config.ImdbConfigProvider
import inc.zhugastrov.imdb.services.impl._
import inc.zhugastrov.imdb.services._

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Config]).toProvider(classOf[ImdbConfigProvider]).asEagerSingleton()
    bind(classOf[TitlesByIdService]).to(classOf[TitlesByIdServiceImpl]).asEagerSingleton()
    bind(classOf[ParentsGuideService]).to(classOf[ParentsGuideServiceImpl]).asEagerSingleton()
    bind(classOf[TitleNameToIdService]).to(classOf[TitleNameToIdServiceImpl]).asEagerSingleton()
    bind(classOf[CastByMovieService]).to(classOf[CastByMovieServiceImpl]).asEagerSingleton()
    bind(classOf[ImdbService]).to(classOf[ImdbServiceImpl]).asEagerSingleton()
  }


}
