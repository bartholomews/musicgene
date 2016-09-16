package model.music

import model.constraints.Constraint

/**
  * A Playlist request contains the name of the playlist, the length,
  * an ordered sequence of String IDs and a Set of Constraints
  */
case class PlaylistRequest(name: String, length: Int, ids: Vector[String], constraints: Set[Constraint])