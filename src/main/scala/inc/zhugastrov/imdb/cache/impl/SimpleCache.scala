package inc.zhugastrov.imdb.cache.impl

import inc.zhugastrov.imdb.cache.Cache

import java.time.{Duration, Instant}
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SimpleCache[K, V](ttl: Duration, cleanupInterval: Duration) extends Cache[K, V] {
  private case class CacheEntry(value: V, expiresAt: Instant)

  implicit val ec: ExecutionContext = global
  private val cache = new ConcurrentHashMap[K, CacheEntry]()

  private def cleanupExpired(): Unit = {
    val now = Instant.now()

    val iter = cache.entrySet().iterator()
    while (iter.hasNext) {
      val entry = iter.next()
      if (!now.isBefore(entry.getValue.expiresAt)) {
        iter.remove()
      }
    }
  }

  // Start background cleanup
  private val cleanupTask: Future[Unit] = Future {
    while (true) {
      try {
        cleanupExpired()
        Thread.sleep(cleanupInterval.toMillis)
      } catch {
        case NonFatal(_) => // ignore errors
      }
    }
  }

  def put(key: K, value: V): V = {
    val expiresAt = Instant.now().plus(ttl)
    cache.put(key, CacheEntry(value, expiresAt))
    value
  }

  def get(key: K): Option[V] = {
    val entry = cache.get(key)
    if (entry != null && Instant.now().isBefore(entry.expiresAt)) {
      Some(entry.value)
    } else {
      cache.remove(key)
      None
    }
  }
}
