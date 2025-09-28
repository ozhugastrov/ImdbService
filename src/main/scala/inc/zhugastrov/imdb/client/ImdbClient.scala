package inc.zhugastrov.imdb.client

import com.google.inject.{AbstractModule, Provides, Singleton}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.service.{RetryFilter, RetryPolicy}
import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.{Backoff, Http, Service}
import com.twitter.util.{Duration, Return, Timer, Try}
import com.typesafe.config.Config

class ImdbClient extends AbstractModule {
  override def configure(): Unit = ()

  @Provides
  @Singleton
  def imdbClient(config: Config): Service[Request, Response] = {
    val baseUrl = config.getString("client.baseUrl")
    val port = config.getString("client.port")
    val retryFrom = Duration.fromMilliseconds(config.getDuration("client.retryFrom").toMillis)
    val retryTo = Duration.fromMilliseconds(config.getDuration("client.retryTo").toMillis)
    val retryAttempts = config.getInt("client.retryAttempts")

    implicit val timer: Timer = DefaultTimer
    val backoffs = Backoff.exponentialJittered(retryFrom, retryTo).take(retryAttempts)
    val retryPolicy = RetryPolicy.backoff[(Request, Try[Response])](backoffs) {
      case (_, Return(resp)) if resp.status == Status.TooManyRequests => true
    }
    val retryFilter = new RetryFilter[Request, Response](retryPolicy, timer, DefaultStatsReceiver)
    val client = Http.client
      .withTls(baseUrl)
      .newService(s"$baseUrl:$port")
    retryFilter.andThen(client)
  }
}

