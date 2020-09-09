package model.music

import model.constraints.Constraint
import model.genetic.Playlist
import play.api.libs.json.{Json, OWrites}

/**
 * A Playlist request contains the name of the playlist, the length,
 * an ordered sequence of String IDs and a Set of Constraints
 */
case class PlaylistRequest(name: String, length: Int, ids: Vector[String], constraints: Set[Constraint])

case class GeneratedPlaylist(name: String, songs: List[AudioTrackResponse])
object GeneratedPlaylist {
  implicit val playlistResponseWrite: OWrites[GeneratedPlaylist] = Json.writes
  def fromPlaylist(name: String, p: Playlist): GeneratedPlaylist =
    GeneratedPlaylist(name, p.songs.map(s => AudioTrackResponse.fromDomain(s)))
}

case class AudioTrackResponse(
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
object AudioTrackResponse {
  implicit val audioTrackResponseWrite: OWrites[AudioTrackResponse] = Json.writes
  def fromDomain(audioTrack: AudioTrack): AudioTrackResponse = AudioTrackResponse(
    title = audioTrack.title,
    previewUrl = audioTrack.preview_url,
    acousticness = audioTrack.acousticness,
    energy = audioTrack.energy,
    liveness = audioTrack.liveness,
    speechiness = audioTrack.speechiness,
    danceability = audioTrack.danceability,
    tempo = audioTrack.tempo,
    loudness = audioTrack.loudness
  )
}
