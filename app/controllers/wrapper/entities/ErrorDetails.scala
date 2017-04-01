package controllers.wrapper.entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

/**
  * @see https://developer.spotify.com/web-api/user-guide/#error-details
  */
trait ErrorDetails

case class AuthError(error: String, message: String) extends ErrorDetails

object AuthError {
  implicit val authErrorReads: Reads[AuthError] = (
    (JsPath \ "error").read[String] and
      (JsPath \ "error_description").read[String]
    ) (AuthError.apply _)
}

case class RegularError(status: Int, message: String) extends ErrorDetails

object RegularError {
  implicit val errorReads: Reads[ErrorDetails] = (
    (JsPath \ "status").read[Int] and
      (JsPath \ "message").read[String]
    ) (RegularError.apply _)
}