package io.bartholomews.musicgene.model.music

import enumeratum.EnumEntry.Lowercase
import enumeratum._
import io.bartholomews.spotify4s.core.entities.AudioFeatures

sealed trait AudioFeature extends EnumEntry with Lowercase {
  def min: Double
  def max: Double
  def extract(af: AudioFeatures): Double
}

object AudioFeature extends PlayLowercaseEnum[AudioFeature] {

  val values: IndexedSeq[AudioFeature] = findValues

  case object Acousticness extends AudioFeature {
    override val min: Double = 0.0
    override val max: Double = 1.0
    override def extract(af: AudioFeatures): Double = af.acousticness.value
  }

  case object Loudness extends AudioFeature {
    override val min: Double = -60.0
    override val max: Double = 0.0
    override def extract(af: AudioFeatures): Double = af.loudness
  }

  case object Tempo extends AudioFeature {
    override val min: Double = 0
    override val max: Double = 240
    override def extract(af: AudioFeatures): Double = af.tempo
  }
}