package io.bartholomews.musicgene.model.music

sealed trait Attribute[A] {
  def value: A
}

sealed trait TextAttribute extends Attribute[String]
sealed trait TimeAttribute extends Attribute[Int]

sealed trait AudioAttribute extends Attribute[Double] {
  def max: Double
  def min: Double
}

sealed trait PercentageAttribute extends AudioAttribute {
  override final val min: Double = 0.0
  override final val max: Double = 1.0
}

case class Acousticness(value: Double) extends PercentageAttribute
case class Danceability(value: Double) extends PercentageAttribute
case class Energy(value: Double) extends PercentageAttribute
case class Instrumentalness(value: Double) extends PercentageAttribute
case class Liveness(value: Double) extends PercentageAttribute
case class Speechiness(value: Double) extends PercentageAttribute
case class Valence(value: Double) extends PercentageAttribute

case class Title(override val value: String) extends TextAttribute
case class Album(override val value: String) extends TextAttribute
case class Artist(override val value: String) extends TextAttribute
case class SongType(value: String) extends TextAttribute
case class PreviewUrl(value: String = "") extends TextAttribute
case class Year(value: Int) extends Attribute[Int]

case class Duration(value: Double, min: Double = 0.0, max: Double = 1800000) extends AudioAttribute
case class Loudness(value: Double, min: Double, max: Double) extends AudioAttribute
case class Tempo(value: Double, min: Double = 0.0, max: Double = 240) extends AudioAttribute

case class Time_Signature(value: Int) extends TimeAttribute
case class Mode(value: Int) extends TimeAttribute
case class Key(value: Int) extends TimeAttribute

