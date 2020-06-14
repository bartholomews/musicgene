package model.music

import model.constraints.Constraint
import model.genetic.Playlist
import play.api.libs.json.{Json, OWrites}

/**
 * A Playlist request contains the name of the playlist, the length,
 * an ordered sequence of String IDs and a Set of Constraints
 */
case class PlaylistRequest(name: String, length: Int, ids: Vector[String], constraints: Set[Constraint])

case class PlaylistResponse(name: String, songs: List[SongResponse])
object PlaylistResponse {
  implicit val playlistResponseWrite: OWrites[PlaylistResponse] = Json.writes
  def fromPlaylist(name: String, p: Playlist): PlaylistResponse =
    PlaylistResponse(name, p.songs.map(s => SongResponse.fromDomain(s)))
}

case class SongResponse(
  title: Option[String],
  previewUrl: Option[String],
  acousticness: Option[String],
  energy: Option[String],
  liveness: Option[String],
  speechiness: Option[String],
  danceability: Option[String],
  tempo: Option[String],
  loudness: Option[String]
)
object SongResponse {
  implicit val songResponseWrite: OWrites[SongResponse] = Json.writes
  def fromDomain(song: Song): SongResponse = SongResponse(
    title = song.title,
    previewUrl = song.preview_url,
    acousticness = song.acousticness,
    energy = song.energy,
    liveness = song.liveness,
    speechiness = song.speechiness,
    danceability = song.danceability,
    tempo = song.tempo,
    loudness = song.loudness
  )
}
