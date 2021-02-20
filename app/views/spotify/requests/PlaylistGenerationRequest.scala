package views.spotify.requests

import io.bartholomews.spotify4s.core.entities.{FullTrack, SpotifyId}
import play.api.libs.json.{Json, Reads}

// https://www.playframework.com/documentation/latest/ScalaForms
case class PlaylistGenerationRequest(
  name: String,
  size: Int,
  tracks: Set[SpotifyId],
  range: Option[Int],
  constraints: Set[SpotifyConstraint]
)

object PlaylistGenerationRequest {
//  implicit val playlistRequestFormat: OFormat[PlaylistRequest] = Json.format
  import io.bartholomews.spotify4s.playJson.codecs._
  implicit val playlistRequestReads: Reads[PlaylistGenerationRequest] = Json.reads[PlaylistGenerationRequest]
//  val form: Form[PlaylistRequest] =
//    Form(
//      mapping(
//        "name" -> nonEmptyText,
//        "length" -> number(min = 1),
//        "range" -> optional(number(min = 0, max = 100))
////      "songs" -> list(songMapping)
//      )(PlaylistRequest.apply)(PlaylistRequest.unapply)
//    )

  def trackRowClassNames(track: FullTrack): String =
    (List("spotify-track-row") ++
      track.id.map(_ => List.empty).getOrElse(List("disabled")) ++
      track.previewUrl.map(_ => List("clickable", "playable")).getOrElse(List.empty)).mkString(" ")
}
