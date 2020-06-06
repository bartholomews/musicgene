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
  def fromPlaylist(p: Playlist): PlaylistResponse =
    PlaylistResponse("WAT", p.songs.toList.map(SongResponse.fromDomain))
}

case class SongResponse(acousticness: Option[String], danceability: Option[String], tempo: Option[String])
object SongResponse {
  implicit val songResponseWrite: OWrites[SongResponse] = Json.writes
  def fromDomain(song: Song): SongResponse = SongResponse(
    acousticness = song.acousticness,
    danceability = song.danceability,
    tempo = song.tempo
  )
}
