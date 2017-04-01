package controllers.wrapper.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, Reads}

case class SimpleTrack
(
artists: List[SimpleArtist],
available_markets: List[String],  // ISO 3166-1 alpha-2 code
disc_number: Int = 1,
duration_ms: Int,
explicit: Boolean,
external_urls: ExternalURL,
href: String,
id: String,
is_playable: Boolean,
linked_from: TrackLink
)

object SimpleTrack {

  implicit val simpleTrackReads: Reads[SimpleTrack] = (
    (JsPath \ "artists").read[List[SimpleArtist]] and
      (JsPath \ "available_markets").read[List[String]] and
      (JsPath \ "disc_number").read[Int] and
      (JsPath \ "duration_ms").read[Int] and
      (JsPath \ "explicit").read[Boolean] and
      (JsPath \ "external_urls").read[ExternalURL] and
      (JsPath \ "href").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "is_playable").read[Boolean] and
      (JsPath \ "linked_from").read[TrackLink]
    )(SimpleTrack.apply _)

}

case class TrackLink
(
external_urls: ExternalURL,
href: String,
id: String,
uri: String
) { val objectType = "track" }

object TrackLink {

implicit val trackLinkReads: Reads[TrackLink] = (
  (JsPath \ "external_urls").read[ExternalURL] and
    (JsPath \ "href").read[String] and
    (JsPath \ "id").read[String] and
    (JsPath \ "uri").read[String]
  )(TrackLink.apply _)

}