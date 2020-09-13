package views.spotify.requests

import controllers.http.codecs.withDiscriminator
import model.{constraints, music}
import play.api.libs.json.{Json, Reads}

sealed trait SpotifyAttributes {
  def toDomain: model.constraints.Constraint[_]
}

object SpotifyAttributes {
  implicit val reads: Reads[SpotifyAttributes] =
    withDiscriminator.reads[SpotifyAttributes]
}

// FIXME: Try to use the model entity straight away
// All songs must include `attribute`
case class IncludeAll(attribute: AttributeRequest) extends SpotifyAttributes {
  override def toDomain: model.constraints.Constraint[_] = attribute match {
    case Acousticness(value) =>
      model.constraints.IncludeAll(music.Acousticness(value))

    case Tempo(value, min, max) =>
      model.constraints.DecreasingTransition(
        lo = 0,
        hi = 10,
        that = music.Tempo(value, min.getOrElse(0.0), max.getOrElse(240))
      )

    case Loudness(value, min, max) =>
      model.constraints.IncludeAll(music.Loudness(value, min.getOrElse(-60.0), max.getOrElse(0.0)))
  }
}
object IncludeAll {
  implicit val reads: Reads[IncludeAll] = Json.reads[IncludeAll]
}

sealed trait AttributeRequest
object AttributeRequest {
  import controllers.http.codecs._
  implicit val reads: Reads[AttributeRequest] =
    withDiscriminator.reads[AttributeRequest]
}

case class Loudness(value: Double, min: Option[Double], max: Option[Double])
    extends AttributeRequest
object Loudness {
  implicit val reads: Reads[Loudness] = Json.reads[Loudness]
}

case class Tempo(value: Double, min: Option[Double], max: Option[Double])
  extends AttributeRequest
object Tempo {
  implicit val reads: Reads[Tempo] = Json.reads[Tempo]
}

case class Acousticness(value: Double) extends AttributeRequest
object Acousticness {
  implicit val reads: Reads[Acousticness] = Json.reads[Acousticness]
}
