package model.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class Copyright(text: String, copyrightType: String)

object Copyright {
  implicit val copyrightReads: Reads[Copyright] = (
    (JsPath \ "text").read[String] and
      (JsPath \ "type").read[String]
    )(Copyright.apply _)
}
