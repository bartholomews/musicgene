package controllers.wrapper.entities

import play.api.libs.json.Reads
import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

case class NewReleases
(
  albums: Page[SimpleAlbum],
  message: Option[String]
)

object NewReleases {

  implicit val newReleasesReads: Reads[NewReleases] = (
    (JsPath \ "albums").read[Page[SimpleAlbum]] and
      (JsPath \ "message").readNullable[String]
    ) (NewReleases.apply _)

}

