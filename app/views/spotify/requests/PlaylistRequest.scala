package views.spotify.requests

import io.bartholomews.spotify4s.entities.{FullTrack, SpotifyId}
import io.bartholomews.musicgene.controllers.http.codecs.SpotifyCodecs.spotifyIdFormat
import play.api.libs.json.{Json, Reads}

// https://www.playframework.com/documentation/latest/ScalaForms
case class PlaylistRequest(
  name: String,
  length: Int,
  tracks: Set[SpotifyId],
  range: Option[Int],
  constraints: Set[SpotifyAttributes]
)

object PlaylistRequest {
//  implicit val playlistRequestFormat: OFormat[PlaylistRequest] = Json.format
  implicit val playlistRequestReads: Reads[PlaylistRequest] = Json.reads[PlaylistRequest]
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
