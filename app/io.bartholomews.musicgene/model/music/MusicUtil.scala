package io.bartholomews.musicgene.model.music

import io.bartholomews.spotify4s.entities.{AudioFeatures, FullTrack}

/**
  *
  */
object MusicUtil {
  def toAudioTrack2(t: FullTrack, af: AudioFeatures): AudioTrack = {
    AudioTrack(
      t.id.map(_.value).getOrElse("N/A"), // .getOrElse(randomUUID().toString
      Set[Attribute[_]](
        PreviewUrl(t.previewUrl.map(_.toString).getOrElse("")),
        Title(t.name),
        Album(t.album.name),
        Artist(t.artists.head.name),
        Duration(t.durationMs),
        Acousticness(af.acousticness.value),
        Danceability(af.danceability.value),
        Energy(af.energy.value),
        Instrumentalness(af.instrumentalness.value),
        Liveness(af.liveness.value),
        Loudness(af.loudness, 0, 0), // FIXME: WTF ? Are tracks values and constraints mixed up ?
        Speechiness(af.speechiness.value),
        Tempo(af.tempo),
        Valence(af.valence.value),
        Time_Signature(af.timeSignature),
        Mode(af.mode.value),
        Key(af.key.value)
      )
    )
  }

  def toAudioTrack(t: FullTrack): AudioTrack = {
    AudioTrack(
      t.id.map(_.value).getOrElse("N/A"), // .getOrElse(randomUUID().toString),
      Set[Attribute[_]](
        PreviewUrl(t.previewUrl.map(_.toString).getOrElse("")),
        Title(t.name),
        Album(t.album.name),
        Artist(t.artists.head.name),
        Duration(t.durationMs)
      )
    )
  }

  def millisecondsToMinutesAndSeconds(timeUnitMs: Int): String = {
    val minutes = (timeUnitMs / 1000) / 60
    val seconds = (timeUnitMs / 1000) % 60
    "%02d".format(minutes) + ":" + "%02d".format(seconds)
  }
}
