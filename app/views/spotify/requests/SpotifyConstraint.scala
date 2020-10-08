package views.spotify.requests

import io.bartholomews.musicgene.model.{constraints, music}
import play.api.libs.json.{Json, Reads}

sealed trait SpotifyConstraint { def toDomain: constraints.Constraint[_] }
object SpotifyConstraint {
  implicit val reads: Reads[SpotifyConstraint] = withDiscriminator.reads[SpotifyConstraint]
}

// FIXME: Try to use the model entity straight away; also indexRange should be a Tuple2
case class Increasing(attribute: SpotifyAttribute, indexRange: List[Int]) extends SpotifyConstraint {
  override def toDomain: constraints.Constraint[_] = attribute match {
    case Acousticness(value) =>
      constraints.IncreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        that = music.Acousticness(value)
      )

    case Tempo(value, min, max) =>
      constraints.IncreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        that = music.Tempo(value, min.getOrElse(0.0), max.getOrElse(240))
      )

    case Loudness(value, min, max) =>
      constraints.IncreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        that = music.Loudness(value, min.getOrElse(-60.0), max.getOrElse(0.0))
      )
  }
}

object Increasing {
  implicit val increasingReads: Reads[Increasing] = Json.reads[Increasing]
}
