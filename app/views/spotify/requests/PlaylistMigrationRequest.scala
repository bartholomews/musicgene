package views.spotify.requests

import io.bartholomews.spotify4s.entities.SpotifyId
import play.api.libs.json.{Json, OFormat}

/*
  A request to clone a playlist to the main user
 */
case class PlaylistMigrationRequest(
  id: SpotifyId,
  name: String,
  public: Boolean,
  collaborative: Boolean,
  description: Option[String],
  // uris: SpotifyUris,
)

object PlaylistMigrationRequest {
  implicit val playlistMigrationRequestFormat: OFormat[PlaylistMigrationRequest] = Json.format
}
