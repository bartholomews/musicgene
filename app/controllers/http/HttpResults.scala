package controllers.http

import cats.Applicative
import io.bartholomews.fsclient.entities.{ErrorBody, ErrorBodyJson, ErrorBodyString}
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest
import views.common.Tab

sealed abstract class HttpResults(tab: Tab) {

  implicit class HttpResponseImplicits[A](httpResponse: HttpResponse[A]) {
    def toResult(responseToResult: A => Result): Result =
      httpResponse.foldBody(errorToResult, responseToResult)

    def toResultF[F[_]: Applicative](responseToResult: A => F[Result])(implicit f: Applicative[F]): F[Result] =
      httpResponse.foldBody(err => f.pure(errorToResult(err)), responseToResult)
  }

  def errorToResult(error: ErrorBody): Result = error match {
    case ErrorBodyJson(value) => badRequest(value.spaces2)
    // TODO: https://github.com/jilen/play-circe
    case ErrorBodyString(value) => badRequest(value)
  }

  def badRequest(message: String): Result = BadRequest(views.html.common.error(message, tab))
}

case object DiscogsHttpResults extends HttpResults(Tab.Discogs)
case object SpotifyHttpResults extends HttpResults(Tab.Spotify)
