package model.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}


case class NewReleases
(
  playlists: Page[Album]
)
  extends SpotifyObject { override val objectType = "featuredPlaylists" }
