package model.constraints

import model.music.Attribute

/**
  *
  */

trait Score {
  val matched: Boolean
  val info: Option[Info]
  /*
  val distance: Option[Double]
  val index: Option[Int]
  val attr: Option[Attribute]
  */
}

case class RangeScore(matched: Boolean, info: Option[Info] = None) extends Score

case class Info(attr: Attribute, index: Int, distance: Double = 0.0)