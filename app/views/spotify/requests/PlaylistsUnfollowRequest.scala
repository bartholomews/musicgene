package views.spotify.requests

import io.bartholomews.spotify4s.entities.SpotifyUserId
import play.api.libs.json.{Json, OFormat}

case class PlaylistsUnfollowRequest(
  userId: SpotifyUserId,
  playlists: List[SpotifyIdRequest]
)

object PlaylistsUnfollowRequest {
  implicit val playlistsUnfollowRequestFormat: OFormat[PlaylistsUnfollowRequest] = Json.format
}
