package model.music

/**
  *
  */
trait Attribute {
  def value: Any
}

trait TextAttribute extends Attribute {
  override def value: String
}

trait TimeAttribute extends Attribute {
  override def value: Int
}
trait AudioAttribute extends Attribute {
  override def value: Double
  val max: Double
  val min: Double
}

trait PercentageAttribute extends AudioAttribute {
  override val min: Double = 0.0
  override val max: Double = 1.0
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
case class Preview_URL(value: String) extends TextAttribute

case class Year(value: Int) extends Attribute

case class Duration(value: Double, min: Double = 0.0, max: Double = 1800000) extends AudioAttribute
case class Loudness(value: Double, min: Double = -60.0, max: Double = 0.0) extends AudioAttribute
case class Tempo(value: Double, min: Double = 0.0, max: Double = 240) extends AudioAttribute

case class Time_Signature(value: Int) extends TimeAttribute
case class Mode(value: Int) extends TimeAttribute
case class Key(value: Int) extends TimeAttribute

