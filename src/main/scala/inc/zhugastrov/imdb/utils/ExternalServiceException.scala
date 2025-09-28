package inc.zhugastrov.imdb.utils

import com.twitter.finagle.http.Response

case class ExternalServiceException(response: Response) extends Exception
