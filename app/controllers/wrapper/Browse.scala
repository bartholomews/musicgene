package controllers.wrapper

import javax.inject.Inject

import logging.AccessLogging
import model.entities.{Album, FeaturedPlaylists}
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  */
class Browse @Inject()(configuration: play.api.Configuration, ws: WSClient)
  extends SpotifyAPI(configuration, ws) with Controller with AccessLogging {

  private final val BROWSE = s"$API_BASE_URL/browse"

  // =====================================================================================================================
  /**
    * https://developer.spotify.com/web-api/get-list-featured-playlists/
    */
  private final val FEATURED_PLAYLISTS = s"$BROWSE/featured-playlists"

  def featuredPlaylists = Action.async {
    withToken(t => getFeaturedPlaylists(t.access_token) map {
      p: FeaturedPlaylists => Ok(p.message)
    })
  }

  def featuredPlaylists(action: FeaturedPlaylists => Result) = Action.async {
    withToken(t => getFeaturedPlaylists(t.access_token) map {
      p: FeaturedPlaylists => action(p)
    })
  }

  def getFeaturedPlaylists(token: String): Future[FeaturedPlaylists] =
    validate[FeaturedPlaylists] {
      logRequest {
        browseFeaturedPlaylists(token)
      }
    }

  private def browseFeaturedPlaylists(token: String): Future[WSResponse] = {
    ws.url(FEATURED_PLAYLISTS)
      .withHeaders(auth_bearer(token))
      .withQueryString(
        "" -> "" // TODO
      )
      .get()
  }

  // ===================================================================================================================
  /**
    * https://developer.spotify.com/web-api/get-list-new-releases/
    */
  private final val NEW_RELEASES = s"$BROWSE/new-releases"

  def newReleases = Action.async {
    withToken(t => getNewReleases(t.access_token) map {
      p: Album => Ok(p.id)
    })
  }

  def newReleases(action: FeaturedPlaylists => Result) = Action.async {
    withToken(t => getFeaturedPlaylists(t.access_token) map {
      p: FeaturedPlaylists => action(p)
    })
  }

  def getNewReleases(token: String): Future[Album] = validate[Album] {
    logRequest {
      getNewReleasesList(token)
    }
  }

  private def getNewReleasesList(token: String): Future[WSResponse] = {
    ws.url(NEW_RELEASES)
      .withHeaders(auth_bearer(token))
      .withQueryString(
        "" -> "" // TODO
      )
      .get()
  }

  // ===================================================================================================================

}
