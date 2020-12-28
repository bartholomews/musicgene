package io.bartholomews.musicgene.controllers.http

import cats.Applicative
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import play.api.libs.json.Json
import play.api.mvc.Results.BadRequest
import play.api.mvc.{AnyContent, Request, Result}
import sttp.client.{DeserializationError, HttpError, ResponseError}
import views.common.Tab

sealed abstract class HttpResults(tab: Tab) {

  implicit class HttpResultResponseImplicits[E, A](httpResponseEntity: Either[ResponseError[E], A]) {
    def toResulto(f: A => Result)(implicit request: Request[AnyContent]): Result =
      httpResponseEntity.fold(errorToResult, f)
  }

  implicit class HttpResponseImplicits[E, A](httpResponse: SttpResponse[ResponseError[E], A]) {
    def toResult(responseToResult: A => Result)(implicit request: Request[AnyContent]): Result =
      httpResponse.body.fold(errorToResult, responseToResult)

    def toResultF[F[_]: Applicative](
      responseToResult: A => F[Result]
    )(implicit f: Applicative[F], request: Request[AnyContent]): F[Result] =
      httpResponse.body.fold(err => f.pure(errorToResult(err)), responseToResult)
  }

  def errorToJsonResult[E](error: ResponseError[E])(implicit request: Request[AnyContent]): Result =
    BadRequest(
      Json.parse(
        error match {
          case HttpError(body, _)            => body
          case DeserializationError(body, _) => body
        }
      )
    )

  def errorToResult[E](error: ResponseError[E])(implicit request: Request[AnyContent]): Result =
    // TODO: https://github.com/jilen/play-circe
    error match {
      case HttpError(body, _)            => badRequest(body)
      case DeserializationError(body, _) => badRequest(body)
    }

  def badRequest(message: String)(implicit request: Request[AnyContent]): Result =
    BadRequest(views.html.common.error(message, tab))
}

case object DiscogsHttpResults extends HttpResults(Tab.Discogs)
case object SpotifyHttpResults extends HttpResults(Tab.Spotify)
