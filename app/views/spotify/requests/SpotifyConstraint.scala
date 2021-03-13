package views.spotify.requests

import io.bartholomews.fsclient.play.codecs.withDiscriminator
import io.bartholomews.musicgene.model.music.AudioFeature
import io.bartholomews.musicgene.model.{constraints, music}
import io.bartholomews.spotify4s.playJson.codecs._
import play.api.libs.json.{Json, Reads}

sealed trait SpotifyConstraint { def toDomain: constraints.Constraint }
object SpotifyConstraint {
  implicit val reads: Reads[SpotifyConstraint] = withDiscriminator.reads[SpotifyConstraint]
}

// FIXME: Try to use the model entity straight away; also indexRange should be a Tuple2
case class Increasing(attribute: SpotifyAttribute, indexRange: List[Int]) extends SpotifyConstraint {
  override def toDomain: constraints.Constraint = attribute match {
    case Acousticness(value) =>
      constraints.IncreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        af = AudioFeature.Acousticness
      )

    case Tempo(value, min, max) =>
      constraints.IncreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        af = AudioFeature.Tempo
      )

    case Loudness(value, min, max) =>
      constraints.IncreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        af = AudioFeature.Loudness
      )
  }
}

case class Decreasing(attribute: SpotifyAttribute, indexRange: List[Int]) extends SpotifyConstraint {
  override def toDomain: constraints.Constraint = attribute match {
    case Acousticness(value) =>
      constraints.DecreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        af = AudioFeature.Acousticness
      )

    case Tempo(value, min, max) =>
      constraints.DecreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        af = AudioFeature.Tempo
      )

    case Loudness(value, min, max) =>
      constraints.DecreasingTransition(
        lo = indexRange.head,
        hi = indexRange.last,
        af = AudioFeature.Loudness
      )
  }
}

object Increasing {
  implicit val reads: Reads[Increasing] = Json.reads[Increasing]
}

object Decreasing {
  implicit val reads: Reads[Decreasing] = Json.reads[Decreasing]
}
