package controllers.wrapper

import javax.inject.Inject

import logging.AccessLogging
import model.entities.FeaturedPlaylists
import play.api.libs.json.{JsError, JsSuccess}
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

  def getFeaturedPlaylists(token: String): Future[FeaturedPlaylists] = get {
    logRequest {
      browseFeaturedPlaylists(token)
    }
  }

  private def get(f: Future[WSResponse]): Future[FeaturedPlaylists] = {
    f map { response =>
      response.json.validate[FeaturedPlaylists] match {
        case JsSuccess(p, _) => p
        case JsError(errors) => throw new Exception(errors.toString) //.head._2.toList.head.messages.toString())
      }
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

}
