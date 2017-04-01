package controllers.wrapper.entities

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
  * @see https://developer.spotify.com/web-api/object-model/#playlist-object-simplified
  */
case class SimplePlaylist
(
  collaborative: Boolean,
  external_urls: ExternalURL,
  href: String,
  id: String,
  images: List[Image],
  name: String,
  owner: User,
  public: Option[Boolean],
  snapshot_id: String,
  tracks: TracksURL,
  objectType: String,
  uri: String
)

object SimplePlaylist {
  implicit val simplePlaylistReads: Reads[SimplePlaylist] = (
    (JsPath \ "collaborative").read[Boolean] and
      (JsPath \ "external_urls").read[ExternalURL] and
      (JsPath \ "href").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "images").read[List[Image]] and
      (JsPath \ "name").read[String] and
      (JsPath \ "owner").read[User] and
      (JsPath \ "public").readNullable[Boolean] and
      (JsPath \ "snapshot_id").read[String] and
      (JsPath \ "tracks").read[TracksURL] and
      (JsPath \ "type").read[String] and
      (JsPath \ "uri").read[String]
    )(SimplePlaylist.apply _)
}



