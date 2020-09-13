package io.bartholomews.musicgene

import io.bartholomews.fsclient.config.UserAgent
import org.http4s.{ParseResult, Uri}
import play.api.mvc.Results._
import play.api.mvc._

package object controllers {
  def requestHost[C](request: Request[C]): String = {
    val scheme = if (request.secure) "https" else "http"
    s"$scheme://${request.host.stripPrefix("/").stripSuffix("/")}"
  }

  def requestUri[C](request: Request[C]): ParseResult[Uri] =
    Uri.fromString(s"${requestHost(request)}/${request.uri.stripPrefix("/")}")

  val userAgent: UserAgent = UserAgent(
    appName = BuildInfo.name,
    appVersion = Some(BuildInfo.version),
    appUrl = BuildInfo.homepage.map(_.toExternalForm)
  )

  def redirect(uri: Uri): Result = Redirect(Call("GET", uri.renderString))
}
