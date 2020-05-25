package views.spotify

import play.api.data._
import play.api.data.Forms._

// https://www.playframework.com/documentation/latest/ScalaForms
case class PlaylistRequest(name: String)

object PlaylistRequest {
  val form: Form[PlaylistRequest] = Form(
    mapping(
      "name" -> nonEmptyText,
    )(PlaylistRequest.apply)(PlaylistRequest.unapply)
  )
}