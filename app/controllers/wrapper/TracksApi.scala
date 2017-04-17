package controllers.wrapper

import javax.inject.Inject

import logging.AccessLogging
import controllers.wrapper.entities._
import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @see https://developer.spotify.com/web-api/track-endpoints/
  */
class TracksApi @Inject()(configuration: play.api.Configuration, ws: WSClient, api: BaseApi) extends AccessLogging {

  private final def ENDPOINT(id: String) = s"${api.BASE_URL}/tracks/$id"

  val logger = Logger("application")

  // =====================================================================================================================
  /**
    * https://developer.spotify.com/web-api/get-track/
    */
  def getTrack(id: String): Future[Track] = api.get[Track](ENDPOINT(id))

  def getPlaylistTracks(href: String): Future[Page[PlaylistTrack]] = {
    api.getWithOAuth[Page[PlaylistTrack]](href)
  }

  def allTracks(href: String): Future[List[Track]] = allPlaylistTracks(href) map {
    p => p.map(pt => pt.track)
  }

  def allPlaylistTracks(href: String): Future[List[PlaylistTrack]] = {
    api.getAll[PlaylistTrack](href => getPlaylistTracks(href))(ENDPOINT(href))
  }

  // ===================================================================================================================
  /*
  private def getNewReleasesList(token: String, query: Option[String] = None): Future[WSResponse] = {
    ws.url(query.getOrElse(NEW_RELEASES))
      .withHeaders(auth_bearer(token))
      .withQueryString(
        "" -> "" // TODO
      )
      .get()
  }
  */

  // ===================================================================================================================

  /**
    *
    * @param track the Spotify ID for a track
    *           (@see https://developer.spotify.com/web-api/user-guide/#spotify-uris-and-ids)
    *
    * @return an AudioFeatures object for the track
    */
  def getAudioFeatures(track: String): Future[AudioFeatures] = api.getWithOAuth[AudioFeatures] {
    s"${api.BASE_URL}/audio-features/$track"
  }

  /*
  def getAudioFeatures(track: Track): Future[AudioFeatures] = track.id match {
    case Some(id) => getAudioFeatures(id)
    case None => Future.failed(new Exception("Track does not have a Spotify ID."))
  }
  */

  /**
    * tracks the Spotify ID for several tracks
    *            (@see https://developer.spotify.com/web-api/get-several-audio-features/)
    *
    * @return a List of AudioFeatures objects for the tracks
    */
  /* TODO
  def getAudioFeatures(tracks: List[String]): Future[List[AudioFeatures]] = api.get("audio_features") {
    s"${api.BASE_URL}/audio-features/?ids=${tracks.mkString(",")}"
  }
  */

}
