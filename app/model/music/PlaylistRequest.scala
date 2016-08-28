package model.music

import model.constraints.Constraint

/**
  *
  */
case class PlaylistRequest(name: String, numberOfTracks: Int, ids: Vector[String], constraints: Set[Constraint])

