package model.constraints

import model.music.{Attribute, AudioAttribute, Song}

/**
  *
  */
object ConstraintsUtil {

  /**
    *
    * @see http://stackoverflow.com/a/16751674
    *
    * @param s
    * @param that
    * @param f
    * @return
    */
  def compare(s: Song, that: AudioAttribute, f: Double => Boolean): Boolean =
    s.attributes.find(a => a.getClass == that.getClass) match {
      case None => false
      case Some(attr) => attr.value match {
        case n: Number => f(n.asInstanceOf[Double])
        case x => throw new Exception(x + ": " + x.getClass + " is not a java.lang.Number")
      }
    }

  def compareEqual(s: Song, that: Attribute): Boolean =
    s.attributes.find(attribute => attribute == that) match {
      case None => false
      case Some(attr) => true
    }

}
