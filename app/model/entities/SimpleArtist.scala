package model.entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class SimpleArtist
(
external_urls: ExternalURL,
href: String,
id: String,
name: String,
uri: String
) { val objectType = "artist" }

object SimpleArtist {
  implicit val simpleArtistReads: Reads[SimpleArtist] = (
    (JsPath \ "external_urls").read[ExternalURL] and
      (JsPath \ "href").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "uri").read[String]
    )(SimpleArtist.apply _)
}
