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
  */
trait Constraint {
  val that: Attribute
  def score(p: Playlist): Seq[Score]
}

case class IncludeAny(that: Attribute) extends Constraint {
  def score(p: Playlist) = Seq(Score(p.songs.exists(s => s.attributes == that)))
}

trait IndexedConstraint extends Constraint {
  val lo: Int; val hi: Int
  def inRange(p: Playlist): Boolean = {
    if (lo >= 0 && hi >= 0 && lo < p.size && hi < p.size && lo <= hi) true
    else throw new IndexOutOfBoundsException("Cannot get index range " + lo + "-" + hi + " of Playlist")
  }
}

// ================================================================================================
// UNARY CONSTRAINT: Can accept `Any` Attribute value
// ================================================================================================

/**
  * Songs from `lo` to `hi` should include `that` Attribute with that.value
  *
  * @param lo
  * @param hi
  * @param that
  */
case class Include(lo: Int, hi: Int, that: Attribute) extends IndexedConstraint {
  override def score(p: Playlist) = {
    for (index <- lo until hi) yield {
      Score(matched = p.songs(index).attributes.contains(that), Some(Info(that, index)))
    }
  }
}

/**
  * Songs from `lo` to `hi` should exclude `that` Attribute with that.value
  *
  * @param lo
  * @param hi
  * @param that
  */
case class Exclude(lo: Int, hi: Int, that: Attribute) extends IndexedConstraint {
  override def score(p: Playlist) = {
    for (index <- lo until hi) yield {
      Score(matched = !p.songs(index).attributes.contains(that), Some(Info(that, index)))
    }
  }
}

// ================================================================================================
// MONOTONIC CONSTRAINT
// ================================================================================================

trait AudioConstraint extends IndexedConstraint {
  val that: AudioAttribute
}

trait MonotonicValue extends AudioConstraint {
  val penalty = Double.MaxValue // this is currently not used, should remove from ConstraintsUtil
  def score(p: Playlist, f: (Double, Double) => Boolean): Seq[Score] = {
    assert(inRange(p))
    for(index <- lo to hi) yield {
      val t = ConstraintsUtil.compare(p.songs(index), that, f)
      Score(t._1, Some(Info(that, index, t._2)))
    }
  }
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
  override def score(p: Playlist) = score(p, (x, y) => x < y)
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
  override def score(p: Playlist) = score(p, (x, y) => x > y)
}

/**
  * Song at position `from` to `to` must include Attribute `y` with value == `that` +- `tolerance`
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param that the attribute the song needs to match
  * @return true if the attribute x of the song matches y, false otherwise
  */
case class IncludeEquals(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicValue {
  override def score(p: Playlist) = {
    assert(inRange(p))
    for (index <- lo to hi) yield {
      val t = ConstraintsUtil.compareWithTolerance(p.songs(index), that)
      Score(t._1, Some(Info(that, index, t._2)))
    }
  }
}

// ================================================================================================
//  MONOTONIC_TRANSITION CONSTRAINTS
// ================================================================================================

/**
  * Favours smoothness between tracks, as it compare each subsequent track value
  */
trait MonotonicTransition extends AudioConstraint {
  // calculate monotonic distance as per f(Double => Boolean)
  def score(p: Playlist, f: (Double, Double) => Boolean) = {
    assert(inRange(p))
    for (index <- lo until hi) yield {
      ConstraintsUtil.extractValues(p.songs(index), p.songs(index + 1), that) match {
        case None => Score(matched = false, Some(Info(that, index)))
        case Some((x, y)) =>
          val t = ConstraintsUtil.monotonicDistance(x, y, that, f(x, y))
          Score(t._1, Some(Info(that, index, t._2)))
      }
    }
  }
}

  /**
    * Songs from index i to index j should have that Attribute value as close as possible
    *
    * @param that its value contains the penalty value: should be higher than any possible distance?
    * @param lo
    * @param hi
    */
  case class ConstantTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
    override def score(p: Playlist) = {
      for (index <- lo until hi) yield {
        val t = ConstraintsUtil.compareWithTolerance(p.songs(index), p.songs(index + 1), that)
        Score(t._1, Some(Info(that, index, t._2)))
      }
      // TODO
      // as all the values will be unmatched, do not "save" all indexes in unmatchedBucke
      //monoScores.map(s => RangeScore(s.matched, Some(Info(that, index, t._2))
    }
  }

    /**
      * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
      *
      * @param that its value contains the penalty value: should be higher than any possible distance?
      * @param lo
      * @param hi
      */
    case class IncreasingTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
      override def score(p: Playlist) = score(p, (x, y) => x < y)
    }

    /**
      * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
      *
      * @param that its value contains the penalty value: should be higher than any possible distance?
      * @param lo
      * @param hi
      */
    case class DecreasingTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
      override def score(p: Playlist) = score(p, (x, y) => x > y)
    }


/*
    case class ExcludeAny(attribute: Attribute) extends Constraint {
      override def calc(p: Playlist) = p.songs.exists(s => !s.attributes.contains(attribute))
    }

    case class ExcludeAll(attribute: Attribute) extends Constraint {
      override def calc(p: Playlist) = !p.songs.exists(s => s.attributes.contains(attribute))
    }

  // def UnaryIncludeAny(i: Int, y: Set[Attribute]): Boolean = y.exists(a => UnaryInclude(i, a))
  // def DurationConstraint(y: Int) = db.songs.flatMap(s => s.attributes.ge)

  // total length, or percentage of presence of an attribute
  case class GlobalSum(i: Int, j: Int, x: Attribute)
  case class GlobalCount(i: Int, j: Int, x: Attribute, minCount: Int, maxCount: Int)

  // TODO relationship between adjacent songs, parts of playlist (e.g. first half/first 10min with Attribute=value)
  // TODO T-S-A: given a start and end song, select the others based on ???

}
*/

// ================================================================================================