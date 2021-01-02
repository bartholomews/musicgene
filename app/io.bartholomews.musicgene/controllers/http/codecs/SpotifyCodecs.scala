package io.bartholomews.musicgene.controllers.http.codecs

import io.bartholomews.spotify4s.core.entities.{SpotifyId, SpotifyUserId}
import play.api.libs.json.{Format, Json}

trait SpotifyCodecs extends CodecsConfiguration {
  implicit val spotifyIdFormat: Format[SpotifyId] = Json.valueFormat[SpotifyId]
  implicit val spotifyUserIdFormat: Format[SpotifyUserId] = Json.valueFormat[SpotifyUserId]
}

object SpotifyCodecs extends SpotifyCodecs {}