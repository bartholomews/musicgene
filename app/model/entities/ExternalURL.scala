package model.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class ExternalURL(key: String, value: String)

object ExternalURL {
  implicit val externalURLReads: Reads[ExternalURL] = (
    (JsPath \ "key").read[String] and
      (JsPath \ "value").read[String]
    )(ExternalURL.apply _)
}
