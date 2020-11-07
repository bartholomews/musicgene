package io.bartholomews.musicgene

import io.bartholomews.fsclient.config.UserAgent
import org.http4s.Uri
import org.http4s.Uri.Path.Segment
import play.api.mvc.Results._
import play.api.mvc._

package object controllers {

  def requestScheme[C](request: Request[C]): Uri.Scheme =
    if (request.secure) Uri.Scheme.https else Uri.Scheme.http

  def requestHost[C](request: Request[C]): Uri.Path =
    Uri.Path(
      segments = Vector(Segment(request.host)),
      absolute = false,
      endsWithSlash = false
    )

  def requestUri[C](request: Request[C]): Uri =
    Uri.apply(
      scheme = Some(requestScheme(request)),
      path = Uri.Path(
        segments = Vector(Segment(request.host), Segment(request.uri)),
        absolute = false,
        endsWithSlash = false
      )
    )

  val userAgent: UserAgent = UserAgent(
    appName = musicgene.BuildInfo.name,
    appVersion = Some(musicgene.BuildInfo.version),
    appUrl = musicgene.BuildInfo.homepage.map(_.toExternalForm)
  )

  def redirect(uri: Uri): Result = Redirect(Call("GET", uri.renderString))
}
