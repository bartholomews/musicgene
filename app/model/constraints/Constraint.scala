package model.constraints

import model.genetic.Playlist
import model.music._

trait Constraint {
  val that: Attribute
  def score(p: Playlist): Seq[Score]
}

case class IncludeAny(that: Attribute) extends Constraint {
  def score(p: Playlist) = Seq(Score(p.songs.exists(s => s.attributes.contains(that))))
}

case class IncludeAll(that: Attribute) extends Constraint {
  def score(p: Playlist) = Seq(Score(p.songs.forall(s => s.attributes.contains(that))))
}

case class ExcludeAny(that: Attribute) extends Constraint {
  def score(p: Playlist) = Seq(Score(p.songs.exists(s => !s.attributes.contains(that))))
}

case class ExcludeAll(that: Attribute) extends Constraint {
  def score(p: Playlist) = Seq(Score(!p.songs.exists(s => s.attributes.contains(that))))
}

trait IndexedConstraint extends Constraint {
  val lo: Int; val hi: Int
  def inRange(p: Playlist): Boolean = {
    if (lo >= 0 && hi >= 0 && lo < p.size && hi < p.size && lo <= hi) true
    else throw new IndexOutOfBoundsException("Cannot get index range " + lo + "-" + hi + " of Playlist with size " + p.size)
  }
}

// ================================================================================================
// UNARY CONSTRAINT: Can accept `Any` Attribute value
// ================================================================================================

/**
  * Songs from `lo` to `hi` should include `that` Attribute with that.value
  */
case class Include(lo: Int, hi: Int, that: Attribute) extends IndexedConstraint {
  override def score(p: Playlist) = {
    for (index <- lo to hi) yield {
      Score(matched = p.songs(index).attributes.contains(that), Some(Info(that, index)))
    }
  }
}

/**
  * Songs from `lo` to `hi` should exclude `that` Attribute with that.value
  */
case class Exclude(lo: Int, hi: Int, that: Attribute) extends IndexedConstraint {
  override def score(p: Playlist) = {
    for (index <- lo to hi) yield {
      Score(matched = !p.songs(index).attributes.contains(that), Some(Info(that, index)))
    }
  }
}

case class AdjacentInclude(lo: Int, hi: Int, that: Attribute) extends IndexedConstraint {
  override def score(p: Playlist) = {
    for(index <- lo until hi) yield {
      val info = Some(Info(that, index))
      ConstraintsUtil.extractValues(p.songs(index), p.songs(index + 1), that) match {
        case None => Score(matched = false, info)
        case Some((x, y)) => Score(x == y, info)
      }
    }
  }
}

case class AdjacentExclude(lo: Int, hi: Int, that: Attribute) extends IndexedConstraint {
  override def score(p: Playlist) = {
    for(index <- lo until hi) yield {
      val info = Some(Info(that, index))
      ConstraintsUtil.extractValues(p.songs(index), p.songs(index + 1), that) match {
        case None => Score(matched = true, info)
        case Some((x, y)) => Score(x != y, info)
      }
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
//


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
  */
case class ConstantTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
  override def score(p: Playlist) = {
    for (index <- lo until hi) yield {
      val t = ConstraintsUtil.compareWithTolerance(p.songs(index), p.songs(index + 1), that)
      Score(t._1, Some(Info(that, index, t._2)))
    }
  }
}

/**
  * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
  */
case class IncreasingTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
  override def score(p: Playlist): Seq[Score] = score(p, (x, y) => x < y)
}

/**
  * Songs from index i to index j should have that Attribute value as close as possible to f(x, y)
  */
case class DecreasingTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
  override def score(p: Playlist): Seq[Score] = score(p, (x, y) => x > y)
}

case class DecreasingTransitionAll(that: AudioAttribute) extends Constraint {
  override def score(p: Playlist): Seq[Score] = DecreasingTransition(0, p.songs.length, that).score(p)
}


// ================================================================================================
// UNARY CONSTRAINT: Can accept `Any` Attribute value
// ================================================================================================

trait RangeConstraint extends AudioConstraint {
  val from: Double
  val to: Double
  def score(p: Playlist, f: Double => Boolean): Seq[Score] = {
    for(index <- lo to hi) yield {
      val info = Some(Info(that, index))
      ConstraintsUtil.extractValue(p.songs(index), that) match {
        case None => Score(matched = false, info)
        case Some(x) => Score(f(x), info)
      }
    }
  }
}

case class InRange(lo: Int, hi: Int, from: Double, to: Double, that: AudioAttribute) extends RangeConstraint {
  override def score(p: Playlist) = score(p, x => x >= from && x <= to)
}

case class OutRange(lo: Int, hi: Int, from: Double, to: Double, that: AudioAttribute) extends RangeConstraint {
  override def score(p: Playlist) = score(p, x => x < from || x > to)
}

// ================================================================================================