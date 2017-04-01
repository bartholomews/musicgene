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
  * @see https://developer.spotify.com/web-api/playlist-endpoints/
  */
class PlaylistsApi @Inject()(configuration: play.api.Configuration, ws: WSClient,
                             api: BaseApi, profiles: ProfilesApi) extends AccessLogging {

  private final def ENDPOINT(userId: String) = s"${api.BASE_URL}/users/$userId/playlists"

  val logger = Logger("application")

  // =====================================================================================================================

  def playlists(userId: String): Future[Page[SimplePlaylist]] = {
    api.getWithOAuth[Page[SimplePlaylist]](ENDPOINT(userId))
  }

  def myPlaylists: Future[Page[SimplePlaylist]] = {
    profiles.me flatMap {
      my => playlists(my.id)
    }
  }

  /*
  def allMyPlaylists: Future[List[SimplePlaylist]] = {
    profiles.me.flatMap {
      my => api.getAll[SimplePlaylist](href => api.getWithOAuth[Page[SimplePlaylist]](href, false))(ENDPOINT(my.id))
    }
  }
  */

  // ===================================================================================================================
  /**
    *
    */
  // ===================================================================================================================

}
