package io.bartholomews.musicgene.model.music

import io.bartholomews.spotify4s.core.entities.{AudioFeatures, FullTrack}

/**
 *
 */
case class AudioTrack private (
  id: Option[String],
  artistsName: List[String],
  albumName: String,
  previewUrl: Option[String],
  title: String,
  duration: Int,
  features: Option[AudioFeatures]
)
object AudioTrack {
  def apply(track: FullTrack, af: Option[AudioFeatures]): AudioTrack = new AudioTrack(
    track.id.map(_.value),
    track.artists.map(_.name),
    track.album.name,
    track.previewUrl.map(_.toString),
    track.name,
    track.durationMs,
    af
  )
}
