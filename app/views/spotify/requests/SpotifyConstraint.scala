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
case class Increasing(attribute: AudioFeature, indexRange: List[Int]) extends SpotifyConstraint {
  override def toDomain: constraints.Constraint =
      constraints.IncreasingTransition(lo = indexRange.head, hi = indexRange.last, af = attribute)
}

case class Decreasing(attribute: AudioFeature, indexRange: List[Int]) extends SpotifyConstraint {
  override def toDomain: constraints.Constraint =
    constraints.DecreasingTransition(lo = indexRange.head, hi = indexRange.last, af = attribute)
}

object Increasing {
  implicit val reads: Reads[Increasing] =
    Json.reads[Increasing]
}

object Decreasing {
  implicit val reads: Reads[Decreasing] = Json.reads[Decreasing]
}
