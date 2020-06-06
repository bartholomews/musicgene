package views.spotify

import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, OFormat}

// https://www.playframework.com/documentation/latest/ScalaForms
case class PlaylistRequest(
  name: String,
  length: Int,
  range: Option[Int]
//                           songs: List[Song]
)

object PlaylistRequest {

  implicit val playlistRequestFormat: OFormat[PlaylistRequest] = Json.format

  val form: Form[PlaylistRequest] =
    Form(
      mapping(
        "name" -> nonEmptyText,
        "length" -> number(min = 1),
        "range" -> optional(number(min = 0, max = 100))
//      "songs" -> list(songMapping)
      )(PlaylistRequest.apply)(PlaylistRequest.unapply)
    )
}
