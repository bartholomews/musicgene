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
  def calc(p: Playlist): Boolean
}

trait RangeConstraint {
  def calc(p: Playlist): List[Boolean]
}

trait UnaryConstraint extends Constraint
trait GlobalConstraint extends Constraint

/*
abstract class IndexConstraint(val index: Int) {}

abstract class OverallConstraint {
  def calc(p: Playlist): Boolean
}
*/

// ================================================================================================

case class UnaryEqualAll(a: Attribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = {
    p.songs.forall(s => s.attributes.contains(a))
  }
}

case class UnaryEqualAny(a: Attribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = {
    p.songs.exists(s => s.attributes.contains(a))
  }
}

case class UnaryEqualNone(a: Attribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = {
    !p.songs.exists(s => s.attributes.contains(a))
  }
}

// all songs in the playlist have Attribute value x > a.value
case class UnaryLargerAll(a: AudioAttribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = p.songs.forall(s => calc(s))
  def calc(s: Song): Boolean = ConstraintsUtil.compare(s, a, x => x > a.value)
}

case class UnaryLargerAny(a: AudioAttribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = p.songs.exists(s => calc(s))
  def calc(s: Song): Boolean = ConstraintsUtil.compare(s, a, x => x > a.value)
}

case class UnaryLargerNone(a: AudioAttribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = !p.songs.exists(s => calc(s))
  def calc(s: Song): Boolean = ConstraintsUtil.compareNone(s, a, x => x > a.value)
}

case class UnarySmallerAll(a: AudioAttribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = p.songs.forall(s => calc(s))
  def calc(s: Song): Boolean = ConstraintsUtil.compare(s, a, x => x < a.value)
}

case class UnarySmallerAny(a: AudioAttribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = p.songs.exists(s => calc(s))
  def calc(s: Song): Boolean = ConstraintsUtil.compare(s, a, x => x < a.value)
}

// no songs have attribute a with value < that.value
case class UnarySmallerNone(a: AudioAttribute) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean = !p.songs.exists(s => calc(s))
  def calc(s: Song): Boolean = ConstraintsUtil.compareNone(s, a, x => x < a.value)
}

/**
  * Song at position `i` must include Attribute `y`
  *
  * @param index the index of the song in the playlist
  * @param attribute the attribute the song needs to match
  * @return true if the attribute x of the song matches y, false otherwise
  */
case class Include(index: Int, attribute: Attribute) extends Constraint {
  override def calc(p: Playlist) = {
    if (index < 0 || index > p.size) false
    else p.songs(index).attributes.contains(attribute)
  }
}


/**
  * Song at position `index` must exclude `that` Attribute value
  *
  * @param attribute
  * @param index
  */
case class Exclude(index: Int, attribute: Attribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    if (index < 0 || index > p.size) false
    else !p.songs(index).attributes.contains(attribute)
  }
}

/**
  * Song at position `i` must include Attribute `y` with value < that
  *
  * @param index the index of the song in the playlist
  * @param that the attribute the song needs to match
  * @return true if the attribute x of the song matches y, false otherwise
  */
case class IncludeSmaller(index: Int, that: AudioAttribute) extends Constraint {
  override def calc(p: Playlist) = {
    if (index < 0 || index > p.size) false
    else ConstraintsUtil.compare(p.songs(index), that, x => x < that.value)
  }
}

/**
  * Song at position `i` must include Attribute `y` with value > that
  *
  * @param index the index of the song in the playlist
  * @param that the attribute the song needs to match
  * @return true if the attribute x of the song matches y, false otherwise
  */
case class IncludeLarger(index: Int, that: AudioAttribute) extends Constraint {
  override def calc(p: Playlist) = {
    if (index < 0 || index > p.size) false
    else ConstraintsUtil.compare(p.songs(index), that, x => x > that.value)
  }
}

// song at index 'i' need to have attribute == a.value +- tolerance
case class IncludeEquals(index: Int, that: AudioAttribute, tolerance: Double) extends UnaryConstraint {
  override def calc(p: Playlist): Boolean =
    ConstraintsUtil.compareWithTolerance(p.songs(index), that, tolerance, x => x == that.value)
}

case class CompareAdjacent(index: Int, that: AudioAttribute, f: Double => Boolean) extends Constraint {
  override def calc(p: Playlist): Boolean =
    ConstraintsUtil.compare(p.songs(index), that, x => f(x))
}





// ================================================================================================

trait ScoreConstraint {
  def distance(p: Playlist): Double

  def calc(p: Playlist, that: AudioAttribute, i: Int, j: Int, f: (Double, Double) => Double): Double = {

  }

  /**
    * Songs from index i to index j should have that Attribute value as close as possible
    *
    * @param that its value contains the penalty value: should be higher than any possible distance?
    * @param i
    * @param j
    */
  case class ConstantRange(that: AudioAttribute, i: Int, j: Int) extends ScoreConstraint {
    override def distance(p: Playlist) = {
      p.songs.sliding(2).map(v => {
        ConstraintsUtil.extractValues(v.head, v.tail.head, that) match {
          case None => that.value
          case Some((x, y)) => ConstraintsUtil.constantDistance(x, y)
        }
      }).sum
    }
  }

  /**
    * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
    *
    * @param that its value contains the penalty value: should be higher than any possible distance?
    * @param i
    * @param j
    */
  case class IncreasingRange(that: AudioAttribute, i: Int, j: Int) extends ScoreConstraint {
    override def distance(p: Playlist) = {
      p.songs.take(j).sliding(2).map(v => {
        ConstraintsUtil.extractValues(v.head, v.tail.head, that) match {
          case None => that.value
          case Some((x, y)) => ConstraintsUtil.monotonicDistance(x, y, that.value, (x, y) => x > y)
        }
      }).sum
    }
  }

  /** TODO AVOID CODE REPETITION
    * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
    *
    * @param that its value contains the penalty value: should be higher than any possible distance?
    * @param i
    * @param j
    */
  case class DecreasingRange(that: AudioAttribute, i: Int, j: Int, f: Double => Boolean) extends ScoreConstraint {
    override def distance(p: Playlist) = {
      p.songs.take(j).sliding(2).map(v => {
        ConstraintsUtil.extractValues(v.head, v.tail.head, that) match {
          case None => that.value
          case Some((x, y)) => ConstraintsUtil.monotonicDistance(x, y, that.value, (x, y) => x < y)
        }
      }).sum
    }
  }

}


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