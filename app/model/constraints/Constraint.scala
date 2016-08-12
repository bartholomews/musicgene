package model.constraints

import model.geneticScala.Playlist
import model.music._

/**
  * Implementation of Constraint Module
  * reference: formal definition in system described in
  * "Constraint-based Playlist Generation by Applying Genetic Algorithm"
  *
  * Unary Operators (UnaryInclude, UnaryExclude, UnaryRange)
  * set the restrictions of attribute values for a single song
  *
  * Binary Operators (BinarySmall, BinaryGreater, BinaryEqual, BinaryNotEqual)
  * set the restrictions of attribute values between two songs
  *
  * Global Operators (GlobalSum, GlobalCount)
  * set the restrictions of attributes values for all songs in the playlist.
  *
  *
  * // TODO general def which takes a f(Double => Double) or something like that to generalise
  */
// db = MusicCollection? Array/Seq[Song]? Something else?

trait Constraint {
  def score(p: Playlist): Set[Score]
}

abstract class RangeConstraint extends Constraint {
  val lo: Int; val hi: Int
  def inRange(p: Playlist): Boolean = {
    if (lo >= 0 && hi >= 0 && lo < p.size && hi < p.size && lo <= hi) true
    else throw new IndexOutOfBoundsException("Cannot get index range " + lo + "-" + hi + " of Playlist")
  }
}

// ================================================================================================
// UNARY CONSTRAINT: Can accept `Any` Attribute value
// ================================================================================================

abstract class UnaryConstraint() extends RangeConstraint {
  val that: Attribute
  // TODO should include penalty in Score to influence GA?
}

/**
  * Songs from `lo` to `hi` should include `that` Attribute with that.value
  *
  * @param lo
  * @param hi
  * @param that
  */
case class Include(lo: Int, hi: Int, that: Attribute) extends UnaryConstraint {
  override def score(p: Playlist) = {
    for (i <- lo until hi) yield {
      Score(matched = p.songs(i).attributes.contains(that), index = i)
    }
  }.toSet
}

/**
  * Songs from `lo` to `hi` should exclude `that` Attribute with that.value
  *
  * @param lo
  * @param hi
  * @param that
  */
case class Exclude(lo: Int, hi: Int, that: Attribute) extends UnaryConstraint {
  override def score(p: Playlist) = {
    for (i <- lo until hi) yield {
      Score(matched = !p.songs(i).attributes.contains(that), index = i)
    }
  }.toSet
}

// ================================================================================================
// MONOTONIC CONSTRAINT
// ================================================================================================

abstract class MonotonicConstraint() extends RangeConstraint {
  val that: AudioAttribute
}

abstract class MonotonicValue() extends MonotonicConstraint {
  val penalty = Double.MaxValue // this is currently not used, should remove from ConstraintsUtil
}

/**
  * Song at position `from` to `to` must include Attribute `y` with value < `that`
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param that the attribute the song needs to match
  * @return true if the attribute x of the song matches y, false otherwise
  */
case class IncludeSmaller(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicValue {
  override def score(p: Playlist) = {
    assert(inRange(p))
    (for (index <- lo to hi) yield {
      val t = ConstraintsUtil.compareDistance(p.songs(index), that, (x, y) => x < y)
      Score(t._1, t._2, index)
    }).toSet
  }
}

/**
  * Song at position `from` to `to` must include Attribute `y` with value > `that`
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param that the attribute the song needs to match
  * @return true if the attribute x of the song matches y, false otherwise
  */
case class IncludeLarger(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicValue {
  override def score(p: Playlist): Set[Score] = {
    assert(inRange(p))
    (for (index <- lo to hi) yield {
      val t = ConstraintsUtil.compareDistance(p.songs(index), that, (x, y) => x > y)
      Score(t._1, t._2, index)
    }).toSet
  }
}

/**
  * Song at position `from` to `to` must include Attribute `y` with value == `that` +- `tolerance`
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param that the attribute the song needs to match
  * @return true if the attribute x of the song matches y, false otherwise
  */
case class IncludeEquals(lo: Int, hi: Int, that: AudioAttribute/*tolerance: Double,*/) extends MonotonicValue {
  override def score(p: Playlist) = {
    assert(inRange(p))
    (for (index <- lo to hi) yield {
   //   val t = ConstraintsUtil.compareEquals(p.songs(index), that, tolerance, penalty)
      val t = ConstraintsUtil.compareDistance(p.songs(index), that, (x, y) => x == y)
      Score(t._1, t._2, index)
    }).toSet
  }
}

// ================================================================================================
//  MONOTONIC_RANGE CONSTRAINTS
// ================================================================================================

/**
  * Favours smoothness between tracks, as it compare each subsequent track value
  */
abstract class MonotonicRange extends MonotonicConstraint {
  // calculate monotonic distance as per f(Double => Boolean)
  def score(p: Playlist, f: (Double, Double) => Boolean) = {
    assert(inRange(p))
    (for (index <- lo until hi) yield {
      ConstraintsUtil.extractValues(p.songs(index), p.songs(index + 1), that) match {
        case None => Score(matched = false, that.value, index)
        case Some((x, y)) =>
          val t = ConstraintsUtil.monotonicDistance(x, y, f(x, y))
          Score(t._1, t._2, index)
      }
    }).toSet
  }
}

  /**
    * Songs from index i to index j should have that Attribute value as close as possible
    *
    * @param that its value contains the penalty value: should be higher than any possible distance?
    * @param lo
    * @param hi
    */
  case class ConstantRange(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicRange {
    override def score(p: Playlist) = score(p, (x, y) => x == y)
  }

      /*
      p.songs.slice(from, to + 1).sliding(2).map(l => {
        ConstraintsUtil.extractValues(l.head, l.tail.head, that) match {
          case None => Score(matched = false, that.value)
          case Some((x, y)) => ConstraintsUtil.constantDistance(x, y)
        }
      }).toSet
    }
    */

    /**
      * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
      *
      * @param that its value contains the penalty value: should be higher than any possible distance?
      * @param lo
      * @param hi
      */
    case class IncreasingRange(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicRange {
      override def score(p: Playlist) = score(p, (x, y) => x < y)
    }

    /**
      * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
      *
      * @param that its value contains the penalty value: should be higher than any possible distance?
      * @param lo
      * @param hi
      */
    case class DecreasingRange(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicRange {
      override def score(p: Playlist) = score(p, (x, y) => x > y)
    }
      /*
      p.songs.slice(from, to + 1).sliding(2).map(v => {
        ConstraintsUtil.extractValues(v.head, v.tail.head, that) match {
          case None => Score(matched = false, that.value, 0)
          case Some((x, y)) => ConstraintsUtil.monotonicDistance(x, y, that.value, x => x > y)
        }
      }).toSet
    }
    */

/*
trait ScoreConstraint {
  def calc(p: Playlist): Score
}

/*
// song at index i have Attribute with that value
case class UnaryInclude(that: Attribute, i: Int) extends ScoreConstraint {
  override def calc(p: Playlist): Boolean = p.songs(i).attributes.exists(a => a.value == that.value)
}
*/

/**
  * `Song` at index i have that `Attribute` value +- tolerance
  *
  * @param that the `Attribute` to be tested against
  * @param i the index of the `Song` to test
  * @param tolerance a Double value defining the acceptable amount of variation from the target result
  *                  to be considered a positive match
  */
case class UnaryIncludeRange(that: AudioAttribute, i: Int, tolerance: Double) extends ScoreConstraint {
  override def calc(p: Playlist): Score = {
    ConstraintsUtil.compareScore(p.songs(i), that, tolerance, (x, y) => x == y)
  }

}


// all songs satisfy
case class IncludeConstraintAll(that: Constraint) extends ScoreConstraint {
  override def calc(p: Playlist): Score = null
}

//
case class UnaryExclude(a: Attribute, i: Int) extends ScoreConstraint {
  override def calc(p: Playlist): Score = null
}



case class UnaryExcludeRange(a: AudioAttribute, i: Int, threshold: Double) extends ScoreConstraint {
  override def calc(p: Playlist): Score = null
}


/*
case class UnarySmaller(index: Int, a: AudioAttribute) extends Constraint {
  override def calc(s: Song): Boolean = s.attributes.find(attr => attr.getClass == a.getClass) match {
    case None => false
    case Some(attr) => attr.value.asInstanceOf[AnyRef] match {
      case n: Number => n.asInstanceOf[Double] < a.value
      case x => throw new Exception(x + ": " + x.getClass + " is not a java.lang.Number")
    }
  }

  override def calc(p: Playlist): Boolean = p.songs
}
*/

/*
// extends WHAT
case class UnaryLarger(index: Int, a: AudioAttribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    p.songs(index).attributes.find(attr => attr.getClass == a.getClass) match {
      case None => false
      case Some(attr) => attr.value.asInstanceOf[AnyRef] match {
        case n: Number => n.asInstanceOf[Double] < a.value
        case x => throw new Exception(x + ": " + x.getClass + " is not a java.lang.Number")
      }
    }
  }
}

case class UnarySmallerAll(a: AudioAttribute) extends ParameterConstraint {
  override def calc(p: Playlist): Boolean = {
    p.songs.indices.forall(i => UnarySmaller(i, a).calc(p))
  }
}
*/


/*
case class UnaryEqualAny(index: Int, a: AudioAttribute) extends Constraint {
  p.songs.exists(s => ConstraintsUtil.compare(s, a, x => x < a.value))
}
*/

/*


    /**
      * All songs must include Attribute `y`
      *
      * @param attribute
      */
    case class IncludeAll(attribute: Attribute) extends Constraint {
      override def calc(p: Playlist) = p.songs.forall(s => s.attributes.contains(attribute))

    }

    case class IncludeAny(attribute: Attribute) extends Constraint {
      override def calc(p: Playlist) = {
        p.songs.exists(s => s.attributes.contains(attribute))
      }
    }

  // TODO change this and that to be a List[Int]
    case class Exclude(attribute: Attribute) extends Constraint {
  }



    case class ExcludeAny(attribute: Attribute) extends Constraint {
      override def calc(p: Playlist) = p.songs.exists(s => !s.attributes.contains(attribute))
    }

    case class ExcludeAll(attribute: Attribute) extends Constraint {
      override def calc(p: Playlist) = !p.songs.exists(s => s.attributes.contains(attribute))
    }

  // def UnaryIncludeAny(i: Int, y: Set[Attribute]): Boolean = y.exists(a => UnaryInclude(i, a))
  // def DurationConstraint(y: Int) = db.songs.flatMap(s => s.attributes.ge)

/*

  case class BinarySmall(i: Int, j: Int, y: TimeAttribute, f: (Attribute, Attribute) => Boolean) {
    def calc = db(i)
  }

  case class BinaryGreater(i: Int, j: Int, y: TimeAttribute)
  case class BinaryEqual(i: Int, j: Int, y: Attribute)

  // total length, or percentage of presence of an attribute
  case class GlobalSum(i: Int, j: Int, x: Attribute)
  case class GlobalCount(i: Int, j: Int, x: Attribute, minCount: Int, maxCount: Int)

  // TODO relationship between adjacent songs, parts of playlist (e.g. first half/first 10min with Attribute=value)
  // TODO T-S-A: given a start and end song, select the others based on ???

}

// ================================================================================================

// TODO
abstract class DerivedConstraint() extends Constraint

/*
 */

// TODO
abstract class UserDefinedConstraint() extends Constraint

*/

*/

*/