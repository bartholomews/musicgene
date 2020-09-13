package io.bartholomews.musicgene.model.constraints

import io.bartholomews.musicgene.model.genetic.Playlist
import io.bartholomews.musicgene.model.music._

/**
  * Top-hierarchy trait for the Constraint module,
  * enforcing to implement an instance of `Attribute`
  * and the score method to evaluate a `Playlist`
  */
trait Constraint[A] {
  def that: Attribute[A]
  def score(p: Playlist): Seq[Score]
}

/**
  * A Playlist should have at least one track with that Attribute value
  *
  * @param that the Attribute to be tested against the Playlist
  */
case class IncludeAny[A](that: Attribute[A]) extends Constraint[A] {
  def score(p: Playlist) = Seq(Score(p.songs.exists((s: AudioTrack) => s.attributes.contains(that))))
}

/**
  * A Playlist should have all tracks with that Attribute value
  *
  * @param that the Attribute to be tested against the Playlist
  */
case class IncludeAll[A](that: Attribute[A]) extends Constraint[A] {
  def score(p: Playlist): Seq[Score] = {
    val numberOfMatches = p.songs.count(s => s.attributes.contains(that))
    Seq(Score(
      matched = numberOfMatches == p.songs.size,
      // info = Some(Info(that, index = -1, distance = numberOfMatches / p.songs.size))
      info = None
    ))
  }
}

/**
  * A Playlist should have at least one track without that Attribute value
  *
  * @param that the Attribute to be tested against the Playlist
  */
case class ExcludeAny[A](that: Attribute[A]) extends Constraint[A] {
  def score(p: Playlist) = Seq(Score(p.songs.exists(s => !s.attributes.contains(that))))
}

/**
  * A Playlist should have all tracks without that Attribute value
  *
  * @param that the Attribute to be tested against the Playlist
  */
case class ExcludeAll[A](that: Attribute[A]) extends Constraint[A] {
  def score(p: Playlist) = Seq(Score(!p.songs.exists(s => s.attributes.contains(that))))
}

/**
  * Constraint trait which enforces upper and lower bounds
  */
trait IndexedConstraint[A] extends Constraint[A] {
  def lo: Int
  def hi: Int
  // check if the bounds are within range for the playlist
  final def inRange(p: Playlist): Boolean = {
    if (lo >= 0 && hi >= 0 && lo < p.size && hi < p.size && lo <= hi) true
    else throw new IndexOutOfBoundsException("Cannot get index range " + lo + "-" + hi + " of Playlist with size " + p.size)
  }
}

/**
  * Tracks from lo to hi should have that Attribute value
  *
  * @param lo the first index of the playlist to test
  * @param hi the last index of the playlist to test
  * @param that the Attribute to be tested against each index within the bounds
  */
case class Include[A](lo: Int, hi: Int, that: Attribute[A]) extends IndexedConstraint[A] {
  override def score(p: Playlist): Seq[Score] = {
    for (index <- lo to hi) yield {
      Score(matched = p.songs(index).attributes.contains(that), Some(Info(that, index)))
    }
  }
}

/**
  * Tracks from lo to hi should not have that Attribute value
  *
  * @param lo the first index of the playlist to test
  * @param hi the last index of the playlist to test
  * @param that the Attribute to be tested against each index within the bounds
  */
case class Exclude[A](lo: Int, hi: Int, that: Attribute[A]) extends IndexedConstraint[A] {
  override def score(p: Playlist) = {
    for (index <- lo to hi) yield {
      Score(matched = !p.songs(index).attributes.contains(that), Some(Info(that, index)))
    }
  }
}

/**
  * Each subsequent track from lo to hi should have that Attribute with same value
  * (the value passed as argument is discarded)
  *
  * @param lo the first index of the playlist to test
  * @param hi the last index of the playlist to test
  * @param that the Attribute to be tested against each index within the bounds
  */
case class AdjacentInclude[A](lo: Int, hi: Int, that: Attribute[A]) extends IndexedConstraint[A] {
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

/**
  * Each subsequent track from lo to hi should not have that Attribute with same value
  * (the value passed as argument is discarded)
  *
  * @param lo the first index of the playlist to test
  * @param hi the last index of the playlist to test
  * @param that the Attribute to be tested against each index within the bounds
  */
case class AdjacentExclude[A](lo: Int, hi: Int, that: Attribute[A]) extends IndexedConstraint[A] {
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

/**
  * Constraint trait which enforces to implement an instance of AudioAttribute
  */
trait AudioConstraint extends IndexedConstraint[Double] {
  override def that: AudioAttribute
}

/**
  * Constraint trait which evaluates a range of tracks against the same value
  */
trait MonotonicValue extends AudioConstraint {
  // evaluate each track within bounds lo and hi (inherited from IndexedConstraint)
  // of Playlist p against that AudioAttribute over the predicate function f
  def score(p: Playlist, f: (Double, Double) => Boolean): Seq[Score] = {
    assert(inRange(p))
    for(index <- lo to hi) yield {
      val t = ConstraintsUtil.compare(p.songs(index), that, f)
      Score(t._1, Some(Info(that, index, t._2)))
    }
  }
}

/**
  * Song at position from lo to hi must include Attribute y with value < that
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
  * Song at position from to to must include Attribute `y` with value == `that` +- `tolerance`
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

/**
  * Trait which compares each adjacent track values
  */
trait MonotonicTransition extends AudioConstraint {
  // evaluate each track within bounds lo and hi - 1 (inherited from IndexedConstraint)
  // of Playlist p against the next track over the predicate function f
  // (that AudioAttribute value is discarded)
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
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param that the AudioAttribute on which each Song in the range is evaluated on
  */
case class ConstantTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
  override def score(p: Playlist) = {
    for (index <- lo until hi) yield {
      // compare the two adjacent songs to have that Attribute value of distance within tolerance
      val t = ConstraintsUtil.compareWithTolerance(p.songs(index), p.songs(index + 1), that)
      Score(t._1, Some(Info(that, index, t._2)))
    }
  }
}

/**
  * Songs from index i to index j should have that Attribute with increasing value
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param that the AudioAttribute on which each Song in the range is evaluated on
  */
case class IncreasingTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
  override def score(p: Playlist): Seq[Score] = score(p, (x, y) => x < y)
}

/**
  * Songs from index i to index j should have that Attribute value with decreasing value
  */
/**
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param that the AudioAttribute on which each Song in the range is evaluated on
  */
case class DecreasingTransition(lo: Int, hi: Int, that: AudioAttribute) extends MonotonicTransition {
  override def score(p: Playlist): Seq[Score] = score(p, (x, y) => x > y)
}

/**
  * All Songs in the playlist should have that Attribute with decreasing value
  *
  * @param that the AudioAttribute on which each Song in the range is evaluated on
  */
case class DecreasingTransitionAll(that: AudioAttribute) extends Constraint[Double] {
  override def score(p: Playlist): Seq[Score] = DecreasingTransition(0, p.songs.length, that).score(p)
}

/**
  * Constraint trait which compares each track within bounds with that AudioAttribute
  * over the predicate function f
  */
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

/**
  * Tracks within lo and hi should have that AudioAttribute between from and to
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param from the minimum AudioAttribute value of the range
  * @param to the maximum AudioAttribute value of the range
  * @param that the AudioAttribute on which each Song in the bounds is evaluated on
  */
case class InRange(lo: Int, hi: Int, from: Double, to: Double, that: AudioAttribute) extends RangeConstraint {
  override def score(p: Playlist) = score(p, x => x >= from && x <= to)
}

/**
  *  Tracks within lo and hi should not have that AudioAttribute between from and to
  *
  * @param lo the lower bound index of the song in the playlist
  * @param hi the upper bound index of the song in the playlist
  * @param from the minimum AudioAttribute value of the range
  * @param to the maximum AudioAttribute value of the range
  * @param that the AudioAttribute on which each Song in the bounds is evaluated on
  */
case class OutRange(lo: Int, hi: Int, from: Double, to: Double, that: AudioAttribute) extends RangeConstraint {
  override def score(p: Playlist) = score(p, x => x < from || x > to)
}

// ================================================================================================