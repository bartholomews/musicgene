package io.bartholomews.musicgene.model.music

object MusicUtil {
  def millisecondsToMinutesAndSeconds(timeUnitMs: Int): String = {
    val minutes = (timeUnitMs / 1000) / 60
    val seconds = (timeUnitMs / 1000) % 60
    "%02d".format(minutes) + ":" + "%02d".format(seconds)
  }
}
