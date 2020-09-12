package model.music

import model.constraints.Constraint
import model.genetic.Playlist
import play.api.libs.json.{Json, OWrites}

/**
  * A Playlist request contains the name of the playlist, the length,
  * an ordered sequence of String IDs and a Set of Constraints
  */
case class PlaylistRequest(name: String,
                           length: Int,
                           ids: Vector[String],
                           constraints: Set[Constraint[_]])

case class GeneratedPlaylist(name: String,
                             songs: List[SpotifyAudioTrackResponse])
object GeneratedPlaylist {
  implicit val playlistResponseWrite: OWrites[GeneratedPlaylist] = Json.writes
  def fromPlaylist(name: String, p: Playlist): GeneratedPlaylist =
    GeneratedPlaylist(
      name,
      p.songs.map(s => SpotifyAudioTrackResponse.fromDomain(s))
    )
}

// FIXME: Don't like this
case class SpotifyAudioTrackResponse(id: String,
                                     artist: Option[String],
                                     album: Option[String],
                                     title: Option[String],
                                     previewUrl: Option[String],
                                     acousticness: Option[String],
                                     energy: Option[String],
                                     liveness: Option[String],
                                     speechiness: Option[String],
                                     danceability: Option[String],
                                     tempo: Option[String],
                                     loudness: Option[String])
object SpotifyAudioTrackResponse {
  implicit val audioTrackResponseWrite: OWrites[SpotifyAudioTrackResponse] =
    Json.writes

  def fromDomain(audioTrack: AudioTrack): SpotifyAudioTrackResponse = {
    SpotifyAudioTrackResponse(
      id = audioTrack.id,
      artist = audioTrack.attributes.collectFirst({ case x: Artist => x.value }),
      album = audioTrack.attributes.collectFirst({ case x: Album   => x.value }),
      title = audioTrack.attributes.collectFirst({ case x: Title   => x.value }),
      previewUrl = audioTrack.attributes.collectFirst({
        case x: PreviewUrl => x.value
      }),
      acousticness = audioTrack.attributes.collectFirst({
        case x: Acousticness => x.value.toString
      }),
      energy = audioTrack.attributes.collectFirst({
        case x: Energy => x.value.toString
      }),
      liveness = audioTrack.attributes.collectFirst({
        case x: Liveness => x.value.toString
      }),
      speechiness = audioTrack.attributes.collectFirst({
        case x: Speechiness => x.value.toString
      }),
      danceability = audioTrack.attributes.collectFirst({
        case x: Danceability => x.value.toString
      }),
      tempo = audioTrack.attributes.collectFirst({
        case x: Tempo => x.value.toString
      }),
      loudness = audioTrack.attributes.collectFirst({
        case x: Loudness => x.value.toString
      })
    )
  }
}
