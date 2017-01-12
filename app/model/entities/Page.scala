package model.entities

import play.api.libs.json.{JsArray, JsValue, Reads}
import play.libs.Json.fromJson

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

/*
object Page {
  implicit def pageReads[T](implicit fmt: Reads[T]): Reads[Page[T]] = new Reads[Page[T]] {
    def reads(json: JsValue): Page[T] = new Page[T](
      (json \ "href").as[String],
      json \ "items" match {
        case JsArray(ts) => ts.map(t => json.as[T](fmt)).toList
        case _ => throw new RuntimeException("Page items object must be a List")
      },
      (json \ "limit").as[Int],
      (json \ "next").asOpt[String],
      (json \ "offset").as[Int],
      (json \ "previous").asOpt[String],
      (json \ "total").as[Int]
    )
  }
}
*/
