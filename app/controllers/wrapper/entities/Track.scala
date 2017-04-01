package controllers.wrapper.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
  * @see https://developer.spotify.com/web-api/object-model/#track-object-simplified
  */
case class Track
(
artists: List[SimpleArtist],
available_markets: Option[List[String]],
disc_number: Int,
duration_ms: Int,
explicit: Option[Boolean],
external_urls: ExternalURL,
href: String,
id: String,
is_playable: Option[Boolean], // track relinking
linked_from: Option[String], // TODO object, track relinking
name: String,
preview_url: Option[String],
track_number: Option[Int],
objectType: String,
uri: String
)


object Track {
  implicit val trackReads: Reads[Track] = (
    (JsPath \ "artists").read[List[SimpleArtist]] and
      (JsPath \ "available_markets").readNullable[List[String]] and
      (JsPath \ "disc_number").read[Int] and
      (JsPath \ "duration_ms").read[Int] and
      (JsPath \ "explicit").readNullable[Boolean] and
      (JsPath \ "external_urls").read[ExternalURL] and
      (JsPath \ "href").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "is_playable").readNullable[Boolean] and
      (JsPath \ "linked_from").readNullable[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "preview_url").readNullable[String] and
      (JsPath \ "track_number").readNullable[Int] and
      (JsPath \ "type").read[String] and
      (JsPath \ "uri").read[String]
    )(Track.apply _)
}
