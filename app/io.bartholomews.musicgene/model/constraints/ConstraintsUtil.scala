package io.bartholomews.musicgene.model.constraints

import io.bartholomews.musicgene.model.music._

/**
  *
  */
object ConstraintsUtil {

  val tolerance = 0.05

//  /**
//    * Extract that Attribute value on the track - FIXME: WTF IS THIS
//    *
//    * @see http://stackoverflow.com/a/16751674
//    * @param s the song whose attribute need to be extracted
//    * @param that the Attribute to extract (its wrapped value is discarded)
//    * @return Some[Double] with the value of the song's Attribute,
//    *         None if the song doesn't have that Attribute
//    */
//  def extractValue[A](s: AudioTrack, that: Attribute[A]): Option[Double] = {
//    s.attributes.find(a => a.getClass == that.getClass) match {
//      case None => None
//      case Some(attr) => attr.value match {
//        case x: Number => Some(x.asInstanceOf[Double])
//        case _ => None
//      }
//    }
//  }

//  /**
//    * Extract that Attribute value on two tracks
//    *
//    * @param s1 the first track
//    * @param s2 the second track
//    * @param that the Attribute to extract (its wrapped value is discarded)
//    * @return Some(Double, Double) with the values of the songs' Attribute,
//    *         None one or both do not have that Attribute
//    */
//  def extractValues[A](s1: AudioTrack, s2: AudioTrack, that: Attribute[A]): Option[(Double, Double)] = {
//    extractValue(s1, that) match {
//      case None => None
//      case Some(x) => extractValue(s2, that) match {
//        case None => None
//        case Some(y) => Some(x, y)
//      }
//    }
//  }

//  /**
//    * Compare a track's value with that AudioAttribute
//    *
//    * @param s the track to compare
//    * @param that the AudioAttribute
//    * @return a tuple for the two values to be within tolerance, and their distance
//    */
//  def compareWithTolerance(s: AudioTrack, that: AudioAttribute): (Boolean, Double) = {
//    extractValue(s, that) match {
//      case None => (false, Double.MaxValue)
//      case Some(x) => compareWithTolerance(x, that.value, that)
//    }
//  }

//  /**
//    * Compare two tracks' values over that AudioAttribute
//    *
//    * @param s1 the first track
//    * @param s2 the second tracks
//    * @param that the AudioAttribute
//    * @return a tuple for the two values to be within tolerance, and their distance
//    */
//  def compareWithTolerance(s1: AudioTrack, s2: AudioTrack, that: AudioAttribute): (Boolean, Double) = {
//    extractValues(s1, s2, that) match {
//      case None => (false, Double.MaxValue)
//      case Some((x, y)) => compareWithTolerance(x, y, that)
//    }
//  }

//  /**
//    * Evaluate the distance between two double values to be within a certain tolerance
//    *
//    * @param x the first value
//    * @param y the second value
//    * @param that the AudioAttribute
//    * @return a tuple for the two values to be within tolerance, and their distance
//    */
//  def compareWithTolerance(x: Double, y: Double, that: AudioAttribute): (Boolean, Double) = {
//    val distance = scala.math.abs(x - y) / (that.max - that.min)
//    (distance <= tolerance, distance)
//  }

//  /**
//    * Compare a track's value with that AudioAttribute over a predicate function f
//    *
//    * @param s the track to evaluate
//    * @param that the AudioAttribute
//    * @param f the predicate function
//    * @return a tuple with the evaluation of f and the distance between the track's value and that value
//    */
//  def compare(s: AudioTrack, that: AudioAttribute, f: (Double, Double) => Boolean): (Boolean, Double) = {
//    extractValue(s, that) match {
//      case None => (false, Double.MaxValue)
//      case Some(x) => monotonicDistance(x, that.value, that, f(x, that.value))
//    }
//  }

//  /**
//    * Compare two tracks' AudioAttribute values over a predicate function f
//    *
//    * @param s1 the first track
//    * @param s2 the second tracks
//    * @param that the AudioAttribute
//    * @param f the predicate function
//    * @return a tuple with the evaluation of f and the distance between the two tracks
//    */
//  def compare(s1: AudioTrack, s2: AudioTrack, that: AudioAttribute, f: (Double, Double) => Boolean): (Boolean, Double) = {
//    extractValues(s1, s2, that) match {
//      case None => (false, Double.MaxValue)
//      case Some((x, y)) => monotonicDistance(x, y, that, f(x, y))
//    }
//  }

  /**
    * Calculate the distance between two values, and return a tuple
    * to facilitate the construction of a Score instance
    *
    * @param x the first value
    * @param y the second value
    * @param that the AudioAttribute
    * @return a tuple for the evaluation of the predicate function,
    *         and the distance between the two values evaluated
    */
  def monotonicDistance(x: Double, y: Double, that: AudioFeature): Double =
    scala.math.abs(x - y) / (that.max - that.min) // fixme: maybe need to do abs of min-max, e.g. for loudness
}