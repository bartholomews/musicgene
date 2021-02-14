package io.bartholomews.musicgene.model.music

import play.api.libs.json.{Json, Reads}

// TODO value should be Refined 0.0 to 1.0
case class MConfidence(value: Double) extends AnyVal
object MConfidence {
  implicit val reads: Reads[MConfidence] = Json.valueReads[MConfidence]
}
