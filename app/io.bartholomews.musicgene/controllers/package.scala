package io.bartholomews.musicgene

import io.bartholomews.fsclient.core.config.UserAgent
import play.api.mvc.Results._
import play.api.mvc._
import sttp.model.Uri

package object controllers {

  val userAgent: UserAgent = UserAgent(
    appName = musicgene.BuildInfo.name,
    appVersion = Some(musicgene.BuildInfo.version),
    appUrl = musicgene.BuildInfo.homepage.map(_.toExternalForm)
  )

  def redirect(uri: Uri): Result = Redirect(Call("GET", uri.toString)) // uri.renderString))
}
