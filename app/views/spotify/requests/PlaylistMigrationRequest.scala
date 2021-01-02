package views.spotify.requests

import io.bartholomews.spotify4s.core.entities.{SpotifyId, SpotifyUserId}
import play.api.libs.json.{Json, OFormat}

case class PlaylistsMigrationRequest(
  userId: SpotifyUserId,
  playlists: List[PlaylistMigrationRequest]
)

object PlaylistsMigrationRequest {
  implicit val playlistsMigrationRequestFormat: OFormat[PlaylistsMigrationRequest] = Json.format
}

/*
  A request to clone a playlist to the main user
 */
case class PlaylistMigrationRequest(
  id: SpotifyId,
  name: String,
  public: Boolean,
  collaborative: Boolean,
  description: Option[String]
)

object PlaylistMigrationRequest {
  implicit val playlistMigrationRequestFormat: OFormat[PlaylistMigrationRequest] = Json.format
}
