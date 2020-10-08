package views.spotify.requests

import io.bartholomews.spotify4s.entities.SpotifyUserId
import play.api.libs.json.{Json, OFormat}

case class PlaylistMigrationRequest(
  userId: SpotifyUserId,
  playlistName: String,
  public: Boolean = true,
  collaborative: Boolean = false,
  description: Option[String] = None,
  uris: List[String]
  // uris: SpotifyUris,
)

object PlaylistMigrationRequest {
  implicit val playlistMigrationRequestFormat: OFormat[PlaylistMigrationRequest] = Json.format
}
