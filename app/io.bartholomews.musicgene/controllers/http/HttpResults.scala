package io.bartholomews.musicgene.controllers.http

import cats.Applicative
import io.bartholomews.fsclient.entities.{ErrorBody, ErrorBodyJson, ErrorBodyString}
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.BadRequest
import play.api.mvc.{AnyContent, Request, Result}
import views.common.Tab

sealed abstract class HttpResults(tab: Tab) {

  implicit class HttpResultResponseImplicits[A](httpResponseEntity: Either[ErrorBody, A]) {
    def toResulto(f: A => Result)(implicit request: Request[AnyContent]): Result =
      httpResponseEntity.fold(errorToResult, f)
  }

  implicit class HttpResponseImplicits[A](httpResponse: HttpResponse[A]) {
    def toResult(responseToResult: A => Result)(implicit request: Request[AnyContent]): Result =
      httpResponse.foldBody(errorToResult, responseToResult)

    def toResultF[F[_]: Applicative](
      responseToResult: A => F[Result]
    )(implicit f: Applicative[F], request: Request[AnyContent]): F[Result] =
      httpResponse.foldBody(err => f.pure(errorToResult(err)), responseToResult)
  }

  def errorToString(error: ErrorBody)(implicit request: Request[AnyContent]): String = error match {
    case ErrorBodyString(value) => value
    // TODO: https://github.com/jilen/play-circe
    case ErrorBodyJson(value) => value.noSpaces
  }

  def errorToJsValue(error: ErrorBody)(implicit request: Request[AnyContent]): JsValue =
    Json.parse(errorToString(error))

  def errorToJsonResult(error: ErrorBody)(implicit request: Request[AnyContent]): Result =
    BadRequest(errorToJsValue(error))

  def errorToResult(error: ErrorBody)(implicit request: Request[AnyContent]): Result = error match {
    case ErrorBodyJson(value) => badRequest(value.spaces2)
    // TODO: https://github.com/jilen/play-circe
    case ErrorBodyString(value) => badRequest(value)
  }

  def badRequest(message: String)(implicit request: Request[AnyContent]): Result =
    BadRequest(views.html.common.error(message, tab))
}

case object DiscogsHttpResults extends HttpResults(Tab.Discogs)
case object SpotifyHttpResults extends HttpResults(Tab.Spotify)
