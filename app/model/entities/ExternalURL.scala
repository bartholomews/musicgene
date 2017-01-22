package model.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class ExternalURL(spotify: String, value: Option[String])

object ExternalURL {
  implicit val externalURLReads: Reads[ExternalURL] = (
    (JsPath \ "spotify").read[String] and
      (JsPath \ "value").readNullable[String]
    )(ExternalURL.apply _)
}
