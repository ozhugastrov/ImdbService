package inc.zhugastrov.imdb.services.impl

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Await, Future}
import inc.zhugastrov.imdb.domain._
import inc.zhugastrov.imdb.utils.ExceptionUtils.getFailedResponse
import inc.zhugastrov.imdb.utils.ExternalServiceException
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TitleNameToIdServiceImplSpec extends AnyFunSuite with Matchers {

  test("return movie IDs for a valid title search") {
    val imdbClient = Mockito.mock(classOf[Service[Request, Response]])
    val searchResponse = Response(Status.Ok)
    searchResponse.contentString = """{"titles":[{"id":"tt1234567"},{"id":"tt7654321"}]}"""
    Mockito.when(imdbClient.apply(any[Request])).thenReturn(Future.value(searchResponse))
    val service = new TitleNameToIdServiceImpl(imdbClient)
    val result = Await.result(service.getMoviesIdByTitle("test"))
    assert(result == List(TitleId("tt1234567"), TitleId("tt7654321")))
  }

  test("handle empty search results") {
    val imdbClient = Mockito.mock(classOf[Service[Request, Response]])
    val searchResponse = Response(Status.Ok)
    searchResponse.contentString = """{"titles":[]}"""
    Mockito.when(imdbClient.apply(any[Request])).thenReturn(Future.value(searchResponse))
    val service = new TitleNameToIdServiceImpl(imdbClient)
    val result = Await.result(service.getMoviesIdByTitle("nonexistentmovie"))
    assert(result == List.empty)
  }

  test("check service down") {
    val imdbClient = Mockito.mock(classOf[Service[Request, Response]])
    Mockito.when(imdbClient.apply(any[Request])).thenReturn(getFailedResponse("API Error"))
    val service = new TitleNameToIdServiceImpl(imdbClient)
    val thrown = intercept[ExternalServiceException] {
      Await.result(service.getMoviesIdByTitle("test"))
    }
    assert(thrown.response.contentString == "API Error")
  }
}
