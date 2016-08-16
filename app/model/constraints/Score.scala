package model.constraints

/**
  *
  */

trait Score {
  val matched: Boolean
  val distance: Option[Double]
  val index: Option[Int]
}

case class RangeScore(matched: Boolean, distance: Option[Double] = Some(0.0), index: Option[Int] = None) extends Score