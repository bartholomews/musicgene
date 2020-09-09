package views.spotify.requests

import controllers.http.codecs.withDiscriminator
import model.music.Confidence
import play.api.libs.json.{Json, Reads}

sealed trait ConstraintRequest
object ConstraintRequest {
  // https://www.playframework.com/documentation/latest/ScalaJsonAutomated
  implicit val reads: Reads[ConstraintRequest] = withDiscriminator.reads[ConstraintRequest]
}

// All songs must include `attribute`
case class IncludeAll(attribute: AttributeRequest) extends ConstraintRequest
object IncludeAll {
  implicit val reads: Reads[IncludeAll] = Json.reads[IncludeAll]
}

sealed trait AttributeRequest
object AttributeRequest {
  import controllers.http.codecs._
  implicit val reads: Reads[AttributeRequest] = withDiscriminator.reads[AttributeRequest]
}

case class Acousticness(confidence: Confidence) extends AttributeRequest
object Acousticness {
  implicit val reads: Reads[Acousticness] = Json.reads[Acousticness]
}
