package controllers

import com.google.inject.Inject
import controllers.wrapper.entities._
import controllers.wrapper.{BaseApi, PlaylistsApi, ProfilesApi, TracksApi}
import logging.AccessLogging
import model.music.Song
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  */
class SpotifyApiController @Inject() (api: BaseApi,
                                      playlistsApi: PlaylistsApi,
                                      profilesApi: ProfilesApi,
                                      tracksApi: TracksApi) extends Controller with AccessLogging {

  /**
    * Redirect a user to authenticate with Spotify and grant permissions to the application
    *
    * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
    */
  def auth = Action {
    Redirect(api.authoriseURL(state = None, scopes = List(), showDialog = false))
  }

  /**
    *
    * @return
    */
  def callback: Action[AnyContent] = Action.async {
    request =>
      request.getQueryString("code") match {
        case Some(code) => try {
          api.callback(code) { _ => hello() }
        } catch {
          case e: Exception => handleException(e)
        }
        case None => request.getQueryString("error") match {
          case Some("access_denied") => Future(BadRequest("You need to authorize permissions in order to use the App."))
          case Some(error) => Future(BadRequest(error))
          case _ => Future(BadRequest("Something went wrong."))
        }
      }
  }

  private def hello() = profilesApi.me map {
    me => Ok(views.html.callback(me.id))
  }

  def myPlaylists: Action[AnyContent] = handleAsync {
    // TODO lookup for a User stored in local db for that spotifyID and fallback to profilesApi
    profilesApi.me flatMap { me =>
      playlistsApi.playlists(me.id).map(p =>
        // TODO store playlist href-name-listOfTracksID for later retrieval
        Ok(views.html.playlists(s"${me.display_name.getOrElse("")} Playlists", p.items))
      )
    }
  }

  /**
    * @param spotifyID the ID of the logged-in user
    * @return
    */
  def playlists(spotifyID: String): Action[AnyContent] = handleAsync {
    // TODO lookup for a User stored in local db for that spotifyID and fallback to profilesApi
    profilesApi.me flatMap { me =>
      playlistsApi.playlists(spotifyID).map(p =>
        // TODO store playlist href-name-listOfTracksID for later retrieval
        Ok(views.html.playlists(s"${me.display_name.getOrElse("")} Playlists", p.items))
      )
    }
  }

  /*
  def playlistTracks(href: String): Action[AnyContent] = handleAsync {
    tracksApi.allTracks(href) map {
      t => Ok(views.html.playlistTracks("OKOOKOOK", t))
    }
  }
  */

  def playlistTracks(href: String): Action[AnyContent] = handleAsync {
    tracksApi.getPlaylistTracks(href) map {
      t => Ok(views.html.playlistTracks("Some playlist", t.items.map(pt => pt.track)))
    }
  }

  def sampleTracks: Action[AnyContent] = handleAsync {
    Future(Ok(views.html.exception("TODO - GET SAMPLE TRACKS FROM DB")))
  }

  private def debug() = {

    /* CAN'T PARSE TRACKS? :(
    val p1: Future[(String, List[Track])] = for {
      playlist1 <- playlistsApi.myPlaylists.map(p => p.items.head)
      myTracks <- tracksApi.getTracks(playlist1.tracks.href)
    } yield (playlist1.name, myTracks.items)
    */

    val playlist: Future[SimplePlaylist] = playlistsApi.myPlaylists.map(p => p.items.head)

    playlist.foreach(p => accessLogger.debug(p.name))

    val tracks: Future[Page[PlaylistTrack]] = playlist.flatMap {
      p => tracksApi.getPlaylistTracks(p.tracks.href)
    }

    tracks.map { p => Ok(views.html.callback(s"${p.items.map(pl => pl.track.name).toString}")) }
  }

  // def newReleases: Future[List[SimpleAlbum]] = spotify.browse.newReleases

  private def handleAsync(block: => Future[Result]): Action[AnyContent] = Action.async {
    try block
    catch {
      case auth: WebApiException => Future(BadRequest(views.html.exception(auth.getMessage)))
      case wtf: AuthError => Future(BadRequest(views.html.exception(wtf.message)))
      case ex: Exception => handleException(ex)
    }
  }

  private def handleException(e: Exception): Future[Result] = {
    accessLogger.debug(e.getMessage)
    Future(BadRequest(s"There was a problem loading this page. Please try again.\n${e.getMessage}"))
    // TODO if e.getMessage is authorization_code_not_provided, you should be able to re-login automatically
    // especially if no_dialog was set to false, anyway even if was set to true could send the user straight there
    // instead of showing the error, or at least showing this error with a link and caching this request to get back
    // as soon as the user has logged in again;
  }

}
