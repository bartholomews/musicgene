package views.spotify.requests

import io.bartholomews.spotify4s.core.entities.SpotifyId
import play.api.libs.json.{Format, Json}

case class SpotifyIdRequest(id: SpotifyId)
object SpotifyIdRequest {
  import io.bartholomews.spotify4s.playJson.codecs._
  implicit val spotifyIdRequestFormat: Format[SpotifyIdRequest] = Json.format
}
