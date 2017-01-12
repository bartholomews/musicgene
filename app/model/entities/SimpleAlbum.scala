package model.entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class SimpleAlbum
(
  album_type: String,
  artists: List[SimpleArtist],
  available_markets: List[String],
  external_urls: ExternalURL,
  href: String,
  id: String,
  images: List[Image],
  uri: String
) extends SpotifyObject { override val objectType = "album" }

object SimpleAlbum {
  implicit val simpleAlbumReads: Reads[SimpleAlbum] = (
    (JsPath \ "album_type").read[String] and
      (JsPath \ "artists").read[List[SimpleArtist]] and
      (JsPath \ "available_markets").read[List[String]] and
      (JsPath \ "external_urls").read[ExternalURL] and
      (JsPath \ "href").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "images").read[List[Image]] and
      (JsPath \ "uri").read[String]
    )(SimpleAlbum.apply _)
}
