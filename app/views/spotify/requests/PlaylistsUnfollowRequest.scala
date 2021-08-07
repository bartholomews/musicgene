package views.spotify.requests

import io.bartholomews.spotify4s.core.entities.SpotifyId.SpotifyUserId
import play.api.libs.json.{Json, OFormat}

case class PlaylistsUnfollowRequest(
  userId: SpotifyUserId,
  playlists: List[SpotifyIdRequest]
)

object PlaylistsUnfollowRequest {
  import io.bartholomews.spotify4s.playJson.codecs._
  implicit val playlistsUnfollowRequestFormat: OFormat[PlaylistsUnfollowRequest] = Json.format
}
