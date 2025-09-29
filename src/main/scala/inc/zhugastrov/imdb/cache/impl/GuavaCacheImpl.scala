package inc.zhugastrov.imdb.cache.impl

import com.google.common.cache.CacheBuilder
import inc.zhugastrov.imdb.cache.Cache

import java.time.Duration

class GuavaCacheImpl[K, V](ttl: Duration) extends Cache[K, V] {
  private val underline = CacheBuilder.newBuilder().expireAfterWrite(ttl).build[K, V]()

  override def put(key: K, value: V): V = {
    underline.put(key, value)
    value
  }

  override def get(key: K): Option[V] = {
    Option(underline.getIfPresent(key))
  }
}
