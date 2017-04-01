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
  *
  */
class BrowseApi @Inject()(configuration: play.api.Configuration, ws: WSClient, api: BaseApi) extends AccessLogging {

  val BASE_URL = configuration.underlying.getString("API_BASE_URL")
  private final val BROWSE = s"$BASE_URL/browse"

  val logger = Logger("application")

  // =====================================================================================================================
  /**
    * https://developer.spotify.com/web-api/get-list-featured-playlists/
    */
  private final val FEATURED_PLAYLISTS = s"$BROWSE/featured-playlists"

  def featuredPlaylistsAction(action: FeaturedPlaylists => Result) = Action.async {
    featuredPlaylists map { p: FeaturedPlaylists => action(p) }
  }

  def featuredPlaylists: Future[FeaturedPlaylists] = api.get[FeaturedPlaylists](FEATURED_PLAYLISTS)

/*
  private def browseFeaturedPlaylists(token: String): Future[WSResponse] = {
    ws.url(FEATURED_PLAYLISTS)
      .withHeaders(api.auth_bearer(token))
      .withQueryString(
        "" -> "" // TODO
      )
      .get()
  }
  */

  // ===================================================================================================================
  /**
    * https://developer.spotify.com/web-api/get-list-new-releases/
    */
  private final val NEW_RELEASES = s"$BROWSE/new-releases"

  def newReleases: Future[List[SimpleAlbum]] = {
    def loop(call: String, acc: List[SimpleAlbum]): Future[List[SimpleAlbum]] = {
      api.get[NewReleases](call) flatMap {
        p: NewReleases =>
          p.albums.next match {
          case None => Future(p.albums.items ::: acc)
          case Some(href) => loop(href, p.albums.items ::: acc)
        }
      }
    }
    loop(NEW_RELEASES, List())
  }

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

}
