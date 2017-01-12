package model.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class TracksURL(href: String, total: Long)

object TracksURL {
  implicit val tracksURLReads: Reads[TracksURL] = (
    (JsPath \ "key").read[String] and
      (JsPath \ "value").read[Long]
    )(TracksURL.apply _)
}
