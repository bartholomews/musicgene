package model.entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class Followers(href: String, total: Int)

object Followers {
  implicit val followersReads: Reads[Followers] = (
    (JsPath \ "href").read[String] and
      (JsPath \ "total").read[Int]
    ) (Followers.apply _)
}
