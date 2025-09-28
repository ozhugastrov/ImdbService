package inc.zhugastrov.imdb


import com.google.inject.{Guice, Injector}
import com.twitter.finagle.Http
import com.twitter.util.Await
import com.typesafe.config.Config
import inc.zhugastrov.imdb.client.ImdbClient
import inc.zhugastrov.imdb.services.ImdbService
import org.slf4j.LoggerFactory


object Main extends App {
  private val logger = LoggerFactory.getLogger(getClass)
  lazy val injector: Injector = Guice.createInjector(List(new Module, new ImdbClient): _*)
  private val conf = injector.getInstance(classOf[Config])
  private val port = conf.getInt("http.port")

  private val imdbService = injector.getInstance(classOf[ImdbService])


  private val server = Http.serve(s":$port", imdbService)
  logger.info("Application started")
  Await.ready(server)
}
