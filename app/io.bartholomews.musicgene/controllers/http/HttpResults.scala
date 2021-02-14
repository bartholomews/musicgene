package io.bartholomews.musicgene.controllers.http

import cats.Applicative
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import play.api.libs.json.Json
import play.api.mvc.Results.BadRequest
import play.api.mvc.{AnyContent, Request, Result}
import sttp.client3.{DeserializationException, HttpError, ResponseException}
import views.common.Tab

sealed abstract class HttpResults(tab: Tab) {

  implicit class HttpResultResponseImplicits[DE, A](httpResponseEntity: Either[ResponseException[String, DE], A]) {
    def toResulto(f: A => Result)(implicit request: Request[AnyContent]): Result =
      httpResponseEntity.fold(errorToResult, f)
  }

  implicit class HttpResponseImplicits[E, A](httpResponse: SttpResponse[E, A]) {
    def toResult(responseToResult: A => Result)(implicit request: Request[AnyContent]): Result =
      httpResponse.body.fold(errorToResult, responseToResult)

    def toResultF[F[_]: Applicative](
      responseToResult: A => F[Result]
    )(implicit f: Applicative[F], request: Request[AnyContent]): F[Result] =
      httpResponse.body.fold(err => f.pure(errorToResult(err)), responseToResult)
  }

  def errorToJsonResult[DE](error: ResponseException[String, DE])(implicit request: Request[AnyContent]): Result =
    BadRequest(
      Json.parse(
        error match {
          case HttpError(body, _)                => body
          case DeserializationException(body, _) => body
        }
      )
    )

  def errorToResult[DE](error: ResponseException[String, DE])(implicit request: Request[AnyContent]): Result =
    // TODO: https://github.com/jilen/play-circe
    error match {
      case HttpError(body, _)                => badRequest(body)
      case DeserializationException(body, _) => badRequest(body)
    }

  def badRequest(message: String)(implicit request: Request[AnyContent]): Result =
    BadRequest(views.html.common.error(message, tab))
}

case object DiscogsHttpResults extends HttpResults(Tab.Discogs)
case object SpotifyHttpResults extends HttpResults(Tab.Spotify)
