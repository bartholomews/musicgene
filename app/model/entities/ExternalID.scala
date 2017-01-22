package model.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, Reads}

case class ExternalID(spotify: String)

object ExternalID {
  /*
  implicit val externalURLReads: Reads[ExternalID] = (
    (JsPath \ "").read[String]
        .orElse(JsPath \ "ss").read[String]
    )(ExternalURL.apply _)
    */

}
