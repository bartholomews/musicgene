package controllers.wrapper.entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

/**
  * @see https://developer.spotify.com/web-api/object-model/#user-object-private
  */
case class UserPrivate
(
birthdate: Option[String],  // field available if `user-read-birthdate` scope is granted
country: Option[String], // ISO 3166-1 alpha-2 field available if `user-read-private` scope is granted
display_name: Option[String],
email: Option[String], // field available if `user-read-email` scope is granted
external_urls: ExternalURL,
followers: Followers,
href: String,
id: String,
images: List[Image],
product: String, // field available if `user-read-private` is granted
objectType: String,
uri: String
)

object UserPrivate {
  implicit val userPrivateReads: Reads[UserPrivate] = (
    (JsPath \ "birthdate").readNullable[String] and
      (JsPath \ "country").readNullable[String] and
      (JsPath \ "display_name").readNullable[String] and
      (JsPath \ "email").readNullable[String] and
      (JsPath \ "external_urls").read[ExternalURL] and
      (JsPath \ "followers").read[Followers] and
      (JsPath \ "href").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "images").read[List[Image]] and
      (JsPath \ "product").read[String] and
      (JsPath \ "type").read[String] and
      (JsPath \ "uri").read[String]
    )(UserPrivate.apply _)
}