package inc.zhugastrov.imdb.services

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}

trait ImdbService extends Service[Request, Response]
