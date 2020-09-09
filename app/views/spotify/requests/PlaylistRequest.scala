package views.spotify.requests

import io.bartholomews.spotify4s.entities.{FullTrack, SpotifyId}
import play.api.libs.json.{Json, OFormat}

// https://www.playframework.com/documentation/latest/ScalaForms
case class PlaylistRequest(
  name: String,
  length: Int,
  tracks: List[SpotifyId],
  range: Option[Int]
//                           songs: List[Song]
)

object PlaylistRequest {
  import controllers.http.codecs.SpotifyCodecs.spotifyIdFormat
  implicit val playlistRequestFormat: OFormat[PlaylistRequest] = Json.format
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
