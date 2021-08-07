package views.spotify.requests

import io.bartholomews.spotify4s.core.entities.SpotifyId
import io.bartholomews.spotify4s.core.entities.SpotifyId.SpotifyUserId
import io.bartholomews.spotify4s.playJson.codecs._
import play.api.libs.json.{Format, Json}

case class PlaylistsMigrationRequest(
  userId: SpotifyUserId,
  playlists: List[PlaylistMigrationRequest]
)

object PlaylistsMigrationRequest {
  implicit val playlistsMigrationRequestFormat: Format[PlaylistsMigrationRequest] = Json.format
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
  implicit val playlistMigrationRequestFormat: Format[PlaylistMigrationRequest] = Json.format
}
