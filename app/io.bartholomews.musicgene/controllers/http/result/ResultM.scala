package io.bartholomews.musicgene.controllers.http.result

import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import io.bartholomews.musicgene.controllers.http.HttpResults
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Request, Result}
import play.twirl.api.Html

sealed trait ResultM {
  def result: Result
}

object ResultM {
  case class Pure(result: Result) extends ResultM
  case class HtmlResult[A](response: HttpResponse[A])(htmlResponse: A => Html, handler: HttpResults) {
    final def result(implicit request: Request[AnyContent]): Result =
      response.entity.fold(handler.errorToResult, entity => Ok(htmlResponse(entity)))
  }
}
