package views.spotify.responses

import io.bartholomews.musicgene.model.genetic.Playlist
import io.bartholomews.musicgene.model.music.AudioTrack
import play.api.libs.json.{Json, OWrites}

case class GeneratedPlaylist(name: String, songs: List[SpotifyAudioTrackResponse])
object GeneratedPlaylist {
  implicit val playlistResponseWrite: OWrites[GeneratedPlaylist] = Json.writes
  def fromPlaylist(name: String, p: Playlist): GeneratedPlaylist =
    GeneratedPlaylist(
      name,
      p.songs.map(s => SpotifyAudioTrackResponse.fromDomain(s))
    )
}

// FIXME: Don't like this
case class SpotifyAudioTrackResponse(
  id: String,
  artist: Option[String],
  album: String,
  title: String,
  previewUrl: Option[String],
  acousticness: Option[String],
  energy: Option[String],
  liveness: Option[String],
  speechiness: Option[String],
  danceability: Option[String],
  tempo: Option[String],
  loudness: Option[String]
)
object SpotifyAudioTrackResponse {
  implicit val audioTrackResponseWrite: OWrites[SpotifyAudioTrackResponse] =
    Json.writes

  def fromDomain(audioTrack: AudioTrack): SpotifyAudioTrackResponse =
    SpotifyAudioTrackResponse(
      id = audioTrack.id.getOrElse(""),
      artist = audioTrack.artistsName.headOption,
      album = audioTrack.albumName,
      title = audioTrack.title,
      previewUrl = audioTrack.previewUrl,
      acousticness = audioTrack.features.map(_.acousticness.value.toString),
      energy = audioTrack.features.map(_.energy.value.toString),
      liveness = audioTrack.features.map(_.liveness.value.toString),
      speechiness = audioTrack.features.map(_.speechiness.value.toString),
      danceability = audioTrack.features.map(_.danceability.value.toString),
      tempo = audioTrack.features.map(_.tempo.toString),
      loudness = audioTrack.features.map(_.loudness.toString)
    )
}
