package views.spotify.requests

import io.bartholomews.spotify4s.core.entities.SpotifyId
import play.api.libs.json.{Json, OFormat}

case class SpotifyIdRequest(id: SpotifyId)
object SpotifyIdRequest {
  implicit val spotifyIdRequestFormat: OFormat[SpotifyIdRequest] = Json.format
}
