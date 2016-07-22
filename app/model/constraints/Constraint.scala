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



/*
  // this constraint is not clear, you give a value() and two bounds?
  case class UnaryRange(index: Int, a: AudioAttribute, from: Double, to: Double) extends ParameterConstraint {
    override def calc(s: Song): Boolean = s.attributes.find(x => x.getClass == a.getClass) match {
      case None => false;
      case Some(attr) =>
        val x = attr.value.asInstanceOf[Double]
        x >= from && x <= to
    }
    override def calc(p: Playlist): Boolean = {
      p.songs(index).attributes.find(x => x.getClass == a.getClass) match {
        case None => false
        case Some(attr) =>
          val x = attr.value.asInstanceOf[Double]
          x >= from && x <= to
      }
    }

*/

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

case class UnaryLargerAll(a: AudioAttribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    p.songs.forall(s => ValueConstraint(a, x => x > a.value).calc(s))
  }
}

case class UnarySmallerNone(a: AudioAttribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    !p.songs.indices.exists(i => UnarySmaller(i, a).calc(p))
  }
}

case class UnaryLargerNone(a: AudioAttribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    !p.songs.indices.exists(i => UnaryLarger(i, a).calc(p))
  }
}

case class UnarySmallerAny(a: AudioAttribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    p.songs.indices.exists(i => UnarySmaller(i, a).calc(p))
  }
}

case class UnaryLargerAny(a: AudioAttribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    p.songs.indices.exists(i => UnaryLarger(i, a).calc(p))
  }
}

case class UnaryEqual(index: Int, a: AudioAttribute) extends Constraint {
  override def calc(p: Playlist): Boolean = {
    p.songs(index).getAttribute(a) match {
      case None => false
      case Some(attr) => attr.asInstanceOf[Double] == a.value
    }
  }
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
    override def calc(s: Song) = !s.attributes.contains(attribute)
  }

  case class Exclude(attribute: Attribute, index: Int) {
    override def calc(p: Playlist, index: Int) = {
      if (index < 0 || index > p.size) false
      else !p.songs(index).attributes.contains(attribute)
    }
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