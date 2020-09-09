package controllers.http.codecs

import io.bartholomews.spotify4s.entities.SpotifyId
import play.api.libs.json.{Format, Json}

object SpotifyCodecs {
  implicit val spotifyIdFormat: Format[SpotifyId] = Json.valueFormat[SpotifyId]
}
