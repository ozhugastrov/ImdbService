package inc.zhugastrov.imdb.utils

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import com.typesafe.config.Config
import inc.zhugastrov.imdb.Main
import inc.zhugastrov.imdb.utils.ExceptionUtils.getFailedResponse

import scala.util.{Failure, Success, Try}

object ClientUtils {

  private val config = Main.injector.getInstance(classOf[Config])
  private val host = config.getString("client.baseUrl")
  def getRequest(url: String, params: Seq[(String, String)] = Seq.empty): Request = {
    val req = Request(url, params: _*)
    req.host = host
    req
  }

  def parseResponse[T](serviceName: String, rawResponse: Response, parseFunc:  => T): Future[T] = {
    if (rawResponse.status != Status.Ok) {
      getFailedResponse(s"IMDB API error for $serviceName: ${rawResponse.status} - ${rawResponse.contentString} - ${rawResponse.headerMap}")
    } else {
      Try {
        parseFunc
      } match {
        case Success(res) => Future.value(res)
        case Failure(ex) =>
          getFailedResponse(s"Failed to parse response for $serviceName: ${ex.getMessage}")
      }
    }
  }

}
