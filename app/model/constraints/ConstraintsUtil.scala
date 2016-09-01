package model.constraints

import model.music._

/**
  *
  */
object ConstraintsUtil {

  val tolerance = 0.05

  /**
    * @see http://stackoverflow.com/a/16751674
    */
  def extractValue(s: Song, that: Attribute): Option[Double] = {
    s.attributes.find(a => a.getClass == that.getClass) match {
      case None => None
      case Some(attr) => attr.value match {
        case x: Number => Some(x.asInstanceOf[Double])
        case _ => None
      }
    }
  }

  def extractValues(s1: Song, s2: Song, that: Attribute): Option[(Double, Double)] = {
    extractValue(s1, that) match {
      case None => None
      case Some(x) => extractValue(s2, that) match {
        case None => None
        case Some(y) => Some(x, y)
      }
    }
  }

  def compareWithTolerance(s: Song, that: AudioAttribute): (Boolean, Double) = {
    extractValue(s, that) match {
      case None => (false, Double.MaxValue)
      case Some(x) => compareWithTolerance(x, that.value, that)
    }
  }

  def compareWithTolerance(s1: Song, s2: Song, that: AudioAttribute): (Boolean, Double) = {
    extractValues(s1, s2, that) match {
      case None => (false, Double.MaxValue)
      case Some((x, y)) => compareWithTolerance(x, y, that)
    }
  }

  def compareWithTolerance(x: Double, y: Double, that: AudioAttribute): (Boolean, Double) = {
    val distance = scala.math.abs(x - y) / (that.max - that.min)
    (distance <= tolerance, distance)
  }

  def compare(s: Song, that: AudioAttribute, f: (Double, Double) => Boolean): (Boolean, Double) = {
    extractValue(s, that) match {
      case None => (false, Double.MaxValue)
      case Some(x) => monotonicDistance(x, that.value, that, f(x, that.value))
    }
  }

  def compare(s1: Song, s2: Song, that: AudioAttribute, f: (Double, Double) => Boolean): (Boolean, Double) = {
    extractValues(s1, s2, that) match {
      case None => (false, Double.MaxValue)
      case Some((x, y)) => monotonicDistance(x, y, that, f(x, y))
    }
  }

  def monotonicDistance(x: Double, y: Double, that: AudioAttribute, f: Boolean): (Boolean, Double) = {
    val distance: Double = scala.math.abs(x - y) / (that.max - that.min)
    (f, distance)
  }

}