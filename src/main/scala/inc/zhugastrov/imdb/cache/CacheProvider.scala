package inc.zhugastrov.imdb.cache

import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.Config
import inc.zhugastrov.imdb.cache.impl.SimpleCache
import inc.zhugastrov.imdb.domain.Movie

class CacheProvider extends AbstractModule {
  override def configure(): Unit = ()


  private def provideCache[K, V](config: Config): SimpleCache[K, V] = {
    val ttl = config.getDuration("cache.ttl")
    val cleanupInterval = config.getDuration("cache.cleanupInterval")
    new SimpleCache[K, V](ttl, cleanupInterval)
  }

  @Provides
  @Singleton
  def requestCache(config: Config): Cache[String, Seq[Movie]] = provideCache[String, Seq[Movie]](config)

  @Provides
  @Singleton
  def movieCache(config: Config): Cache[String, Movie] = provideCache[String, Movie](config)
}
