package model.entities

import play.api.libs.json.Reads
import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

case class Page[T]
(
href: String,
items: List[T],
limit: Int,
next: Option[String],
offset: Int,
previous: Option[String],
total: Int
)

object Page {

  implicit val featuredPlaylistsReads: Reads[Page[SimplePlaylist]] = (
    (JsPath \ "href").read[String] and
      (JsPath \ "items").read[List[SimplePlaylist]] and
      (JsPath \ "limit").read[Int] and
      (JsPath \ "next").readNullable[String] and
      (JsPath \ "offset").read[Int] and
      (JsPath \ "previous").readNullable[String] and
      (JsPath \ "total").read[Int]
    ) (Page.apply[SimplePlaylist] _)

  /*
  implicit def pageReads[T](implicit fmt: Reads[T]): Reads[Page[T]] = new Reads[Page[T]] {
    def reads(json: JsValue): Page[T] = new Page[T](
      (json \ "href").as[String],
      (json \ "items") match {
        case JsDefined(JsArray(ts)) => ts.map(t => json.as[T](fmt)).toList
        case _ => throw new RuntimeException("Page items object must be a List")
      },
      (json \ "limit").as[Int],
      (json \ "next").asOpt[String],
      (json \ "offset").as[Int],
      (json \ "previous").asOpt[String],
      (json \ "total").as[Int]
    )
  }
  */

}

