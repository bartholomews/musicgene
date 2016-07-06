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
  */
// db = MusicCollection? Array/Seq[Song]? Something else?
trait Constraint {
  def calc(p: Playlist): Boolean
}

abstract class ParameterConstraint extends Constraint


  // ================================================================================================

  /**
    * Song at position `i` must include Attribute `y`
    *
    * @param i the index of the song in the playlist
    * @param y the attribute the song needs to match
    * @return true if the attribute x of the song matches y, false otherwise
    */
  case class Include(i: Int, y: Attribute) extends ParameterConstraint {
    override def calc(p: Playlist) = {
      if (i < 0 || i > p.size) false
      else p.songs(i).attributes.contains(y)
    }
  }

    /**
      * All songs must include Attribute `y`
      *
      * @param y
      */
    case class IncludeAll(y: Attribute) extends ParameterConstraint {
      override def calc(p: Playlist) = p.songs.forall(s => s.attributes.contains(y))
    }

    case class IncludeAny(y: Attribute) extends ParameterConstraint {
      override def calc(p: Playlist) = {
        p.songs.exists(s => s.attributes.contains(y))
      }
    }

    case class Exclude(i: Int, y: Attribute) extends ParameterConstraint {
      override def calc(p: Playlist) = {
        if (i < 0 || i > p.size) false
        else !p.songs(i).attributes.contains(y)
      }
    }

    case class ExcludeAny(y: Attribute) extends ParameterConstraint {
      override def calc(p: Playlist) = p.songs.exists(s => !s.attributes.contains(y))
    }

    case class ExcludeAll(y: Attribute) extends ParameterConstraint {
      override def calc(p: Playlist) = !p.songs.exists(s => s.attributes.contains(y))
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