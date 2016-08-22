package model.constraints

import model.music.{Attribute, AudioAttribute, Song}

/**
  *
  */
object ConstraintsUtil {

  /**
    *
    */
  def compare(x: Double, that: AudioAttribute, tolerance: Double): Boolean = {
    val distance = scala.math.abs(x - that.value)
    tolerance - distance >= 0
  }

  def compare(x: Double, that: AudioAttribute, f: Double => Boolean): Boolean = {
    f(x)
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

  def isWithinDistance(x: Double, y: Double, tolerance: Double): Boolean = {
    val distance = scala.math.abs(x - y)
    tolerance - distance >= 0
  }


  /*
  // ==============================================================================
*/

  def extractValues(s1: Song, s2: Song, that: Attribute): Option[(Double, Double)] = {
    extractValue(s1, that) match {
      case None => None
      case Some(x) => extractValue(s2, that) match {
        case None => None
        case Some(y) => Some(x, y)
      }
    }
  }

  /**
    * @see http://stackoverflow.com/a/16751674
    * @param s
    * @param that
    * @return
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

  // NOT USED, CONSIDER TO REMOVE
  def compareWithTolerance(s: Song, that: AudioAttribute, tolerance: Double, penalty: Double): (Boolean, Double) = {
    extractValue(s, that) match {
      case None => (false, penalty)
      case Some(x) =>
        val distance = scala.math.abs(x - that.value)
        if (distance <= tolerance) (true, 0.0) else (false, distance) // + penalty)
    }
  }

  def compareDistance(s: Song, that: AudioAttribute, f: (Double, Double) => Boolean): (Boolean, Double) = {
    extractValue(s, that) match {
      case None => (false, Double.MaxValue)
      case Some(x) => // if(f(x)) 0.0 else
        monotonicDistance(x, that.value, that, f(x, that.value))
    }
  }

  /**
    * TODO insert step Double value in that(value) attribute
    * TODO in that case, the value is the "step"
    * TODO e.g. IncreasingRange(Tempo(10))
    * TODO with Tempo(8), Tempo(9) should be true, 9 (i.e. abs(step - distance))
    * TODO with Tempo(8), Tempo(19) should be true, 1 (i.e. abs(step - distance))
    * TODO with Tempo(10), Tempo(5) should be false, 25 (i.e. step + distance)
    * TODO with Tempo(20), Tempo(2) should be false, 28 (i.e. step + distance)
    *
    * @param x
    * @param y
    * @param f
    * @return
    */
  def monotonicDistance(x: Double, y: Double, that: AudioAttribute, f: Boolean): (Boolean, Double) = {
    // distance rounded to the nearest hundredth: TODO some features might need higher precision
    val distance: Double = scala.math.abs(x - y) / (that.max - that.min)
    // if y is <= of x for Increasing and vice-versa for Decreasing,
    // the distance is added to a penalty value to impact a negative score
    if (!f) (false, distance) // + penalty)
    else (true, distance) // distance)
  }

}
