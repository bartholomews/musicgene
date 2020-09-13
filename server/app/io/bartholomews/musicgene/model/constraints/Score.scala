package io.bartholomews.musicgene.model.constraints

/**
  * A Score result from an evaluation of a constraint
  *
  * @param matched true if it's a matched Score, false otherwise
  * @param info optional information about the evaluation, default at None
  */
case class Score(matched: Boolean, info: Option[Info[_]] = None)

