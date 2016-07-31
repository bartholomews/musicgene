package model.constraints

import model.music.{Attribute, AudioAttribute, Song}

/**
  *
  */
object ConstraintsUtil {

  /**
    *
    * @see http://stackoverflow.com/a/16751674
    * @param s
    * @param that
    * @param f
    * @return
    */
  def compare(s: Song, that: AudioAttribute, f: Double => Boolean): Boolean =
  s.attributes.find(a => a.getClass == that.getClass) match {
    case None => false
    case Some(attr) => attr.value match {
      case x: Number => f(x.asInstanceOf[Double])
      case z => throw new Exception(z + ": " + z.getClass + " is not a java.lang.Number")
    }
  }

  def compareNone(s: Song, that: AudioAttribute, f: Double => Boolean): Boolean = {
    s.attributes.find(a => a.getClass == that.getClass) match {
      case None => true
      case Some(attr) => attr.value match {
        case x: Number => f(x.asInstanceOf[Double])
        case z => throw new Exception(z + ": " + z.getClass + " is not a java.lang.Number")
      }
    }
  }

  def compareWithTolerance(s: Song, that: AudioAttribute, tolerance: Double, f: Double => Boolean): Boolean = {
    s.attributes.find(a => a.getClass == that.getClass) match {
      case None => false
      case Some(attr) => attr.value match {
        case x: Number =>
          // if f => x == y (dynamically refactor for others)
          isWithinDistance(x.asInstanceOf[Double], that.value, tolerance)
        case z => throw new Exception(z + ": " + z.getClass + " is not a java.lang.Number")
      }
    }
  }

  private def isWithinDistance(x: Double, y: Double, tolerance: Double): Boolean = {
    val distance = scala.math.abs(x - y)
    tolerance - distance >= 0
  }


/*
  // SCORE APPROACH FAIL
  // ==============================================================================
*/

  def extractValues(s1: Song, s2: Song, that: AudioAttribute): Option[(Double, Double)] = {
    def extractValue(s: Song): Option[Double] = {
      s.attributes.find(a => a.getClass == that.getClass) match {
        case None => None
        case Some(attr) => attr.value match {
          case x: Number => Some(x.asInstanceOf[Double])
          case z => None
        }
      }
    }
    extractValue(s1) match {
      case None => None
      case Some(x) => extractValue(s2) match {
        case None => None
        case Some(y) => Some(x, y)
      }
    }
  }

  def calculateDistance(f: (Double, Double) => Boolean) = ???

  // as equals as possible, TODO penalty?
  def constantDistance(x: Double, y: Double) = monotonicDistance(x, y, 0.0, (x, y) => x == y)

  // either increasing or decreasing
  def monotonicDistance(x: Double, y: Double, penalty: Double, f: (Double, Double) => Boolean) = {
    // distance rounded to the nearest hundredth: TODO some features might need higher precision
    val distance = scala.math.abs(x - y)
    println("DISTANCE: " + distance)
    // if y is <= of x for Increasing and vice-versa for Decreasing,
    // the distance is added to a penalty value to impact a negative score
    if(!f(x, y)) {
      println(penalty + " + " + distance + " = " + penalty + distance)
      penalty + distance
    } else distance

  }

}


  /*
  // this MIGHT work for equals f, but what about > and < ?
  def compareScore(s: Song, that: AudioAttribute, tolerance: Double, f: (Double, Double) => Boolean): Score = {
    s.attributes.find(a => a.getClass == that.getClass) match {
      case None => Score(matched = false, 1)  // 1 to low, could be even a good score
      case Some(attr) => attr.value match {
        case x: Number => calculateScoreEquals(x.asInstanceOf[Double], that.value, tolerance, f)
        case z => throw new Exception(z + ": " + z.getClass + " is not a java.lang.Number")
      }
    }
  }

  // nononononno
  private def calculateScoreEquals(x: Double, y: Double, tolerance: Double, f: (Double, Double) => Boolean) = {
    val cost = scala.math.abs(x - y); val fitness = tolerance - cost
    if(fitness >= 0) Score(matched = true, cost)
    else Score(matched = false, cost)
  }

  // ScoreGreater should have a worst fitness the more is greater than the value, and be double that bad going other way
  // so that a 1.2 > 1.1 should be the best match, 1.3 > 1.1 should have cost 0.1; 0.8 > 1.0 false(0.3)
  private def calculateScoreGreatLess(x: Double, y: Double, f: (Double, Double) => Boolean) = {
    val fitness =
    if(f(x,y)) Score(matched = false, y + 0.1 - x)
    else Score(matched = true, y - x + 0.1)

  }

  // cost = 2.2

}

*/
