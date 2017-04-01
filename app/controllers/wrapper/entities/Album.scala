package controllers.wrapper.entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class Album
(
album_type: String,
artists: List[SimpleArtist],
available_markets: List[String],
copyrights: List[Copyright],
external_ids: ExternalID,
external_urls: ExternalURL,
genres: List[String],
href: String,
id: String,
images: List[Image],
label: String,
name: String,
popularity: Int,
release_date: String,
release_date_precision: String,
tracks: Page[SimpleTrack],
uri: String
) { val objectType = "album" }

object Album {
  implicit val albumReads: Reads[Album] = (
    (JsPath \ "album_type").read[String] and
      (JsPath \ "artists").read[List[SimpleArtist]] and
      (JsPath \ "available_markets").read[List[String]] and
      (JsPath \ "copyrights").read[List[Copyright]] and
      (JsPath \ "external_ids").read[ExternalID] and
      (JsPath \ "external_urls").read[ExternalURL] and
      (JsPath \ "genres").read[List[String]] and
      (JsPath \ "href").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "images").read[List[Image]] and
      (JsPath \ "label").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "popularity").read[Int] and
      (JsPath \ "release_date").read[String] and
      (JsPath \ "release_date_precision").read[String] and
      (JsPath \ "tracks").read[Page[SimpleTrack]] and
      (JsPath \ "uri").read[String]
    )(Album.apply _)
}
