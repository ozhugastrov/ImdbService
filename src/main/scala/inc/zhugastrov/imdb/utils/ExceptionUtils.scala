package inc.zhugastrov.imdb.utils

import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

object ExceptionUtils {
  def getFailedResponse[A](contentString: String): Future[A] = {
    val response = Response(Status.InternalServerError)
    response.contentString = contentString
    Future.exception(ExternalServiceException(response))
  }

}
