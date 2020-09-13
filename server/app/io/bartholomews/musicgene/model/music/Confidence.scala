package io.bartholomews.musicgene.model.music

import play.api.libs.json.{Json, Reads}

// TODO value should be Refined 0.0 to 1.0
case class Confidence(value: Double) extends AnyVal
object Confidence {
  implicit val reads: Reads[Confidence] = Json.valueReads[Confidence]
}
