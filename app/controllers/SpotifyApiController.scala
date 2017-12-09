package controllers

import utils.FutureUtils
import com.google.inject.Inject
import it.turingtest.spotify.scala.client.logging.AccessLogging
import it.turingtest.spotify.scala.client._
import it.turingtest.spotify.scala.client.entities.{RegularError, WebApiException}
import model.music.{MusicUtil, Song}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  */
class SpotifyApiController @Inject() (api: BaseApi,
                                      authApi: AuthApi,
                                      playlistsApi: PlaylistsApi,
                                      profilesApi: ProfilesApi,
                                      tracksApi: TracksApi) extends Controller with AccessLogging {

  /**
    * Redirect a user to authenticate with Spotify and grant permissions to the application
    *
    * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
    */
  def auth = Action {
    Redirect(authApi.authoriseURL(state = None, scopes = List(), showDialog = false))
  }

  /**
    *
    * @return
    */
  def callback: Action[AnyContent] = Action.async {
    request =>
      request.getQueryString("code") match {
        case Some(code) => try {
          api.setAuthAndThen(code) { _ => hello() }
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

  /**
    * @param user_id the ID of the logged-in user
    * @return the FIRST PAGE of a user playlists (TODO)
    */
  def playlists(user_id: String): Action[AnyContent] = handleAsync {
    // TODO lookup for a User stored in local db for that spotifyID and fallback to profilesApi
    // profilesApi.me flatMap { me =>
    playlistsApi.playlists(user_id).map(p =>
      // TODO store playlist href-name-listOfTracksID for later retrieval
      Ok(views.html.playlists("Playlists", p.items))
    )
    // }
  }

  def myPlaylists2: Action[AnyContent] = handleAsync {
    profilesApi.me flatMap {
      me => playlistsApi.playlists(me.id) map { p => Ok(views.html.playlists(s"${me.id} playlists", p.items)) }
    }
  }

  /*
  TODO This throws either 401 Unauthorised or 429 Too many requests, while playlists(user_id) below works ok
  maybe it depends on the grant requested which are not enough?
   */
  def myPlaylists: Action[AnyContent] = handleAsync {
    profilesApi.myPlaylists map {
      p => Ok(views.html.playlists("My Playlists", p.items))
    } recover {
      case e: RegularError => BadRequest(views.html.exception(s"{'status':${e.status}, 'message':${e.message}}"))
      case ex: Throwable => BadRequest(views.html.exception(ex.getMessage))
    }
  }

  /*
  def myPlaylists: Action[AnyContent] = handleAsync {
    // TODO lookup for a User stored in local db for that spotifyID and fallback to profilesApi
    profilesApi.me flatMap { me =>
      playlistsApi.playlists(me.id).map(p =>
        // TODO store playlist href-name-listOfTracksID for later retrieval
        Ok(views.html.playlists(s"${me.display_name.getOrElse("")} Playlists", p.items))
      )
    }
  }
  */

  /*
  def songTest(href: String): Action[AnyContent] = handleAsync {
    playlistsApi.myPlaylists flatMap { p =>
      val playlist = p.items.head
        song2(playlist.name, playlist.tracks.href) map {
          s => {
            val result: List[(String, List[Song])] = List((s._1, s._2 :: Nil))
            Ok(views.html.tracks("TEST TRACK", result))
          }
        }
    }
  }
  */

  /*
  // TODO I THINK THIS DOESNT WORK FOR A ROUTE NESTING HTTP HREF
  def playlistTracks(href: String): Action[AnyContent] = handleAsync {
    playlistsApi.get(href) flatMap { p =>
      tracksApi.getPlaylistTracks(p.href) map {
        t => Ok(views.html.playlistTracks(p.name, t.items.map(pt => pt.track)))
      }
    }
  }
  */

  def playlistTracks(): Action[AnyContent] = handleAsync {
      profilesApi.myPlaylists flatMap { p =>
        playlistsApi.playlist(p.items.head.owner.id, p.items.head.id) map {
          p => Ok(views.html.playlistTracks(p.name, List()))
        }
    }
    /*
      playlistsApi.tracks(user_id, playlist_id) map {
        t => Ok(views.html.playlistTracks(p.name, t.items.map(pt => pt.track)))
      }
      */
  }

  def songs(user_id: String, playlist_id: String): Action[AnyContent] = handleAsync {
    playlistsApi.playlist(user_id, playlist_id) flatMap { p =>
      val f: Seq[Future[Song]] = p.tracks.items.map(pt =>
        pt.track.id match {
          case Some(id) => tracksApi.getAudioFeatures(id) map { af => MusicUtil.toSong(pt.track, af) }
          case None => Future(MusicUtil.toSong(pt.track))
        })

      FutureUtils.collectFutures(f) map {
        s => Ok(views.html.tracks(s"${p.name}'s tracks", List((p.name, s))))
      }
    }
  }

  /*
  private def toSong(playlist_id: String): Future[List[(Option[String], List[Song])]] = {
    val f: List[Future[Song]] = page.items.map(pt =>
      tracksApi.getAudioFeatures(pt.track.id.get) map {
        af => MusicUtil.toSong(pt.track, af)
      })
    api.getFutureList[Song](f)
  }
  */

//  private def getPlaylistSongs(playlist_href: String): Future[List[Song]] = {
//    api.get(playlist_href) flatMap { page =>
//      val f: List[Future[Song]] = page.items.map(pt =>
//        tracksApi.getAudioFeatures(pt.track.id.get) map {
//          af => MusicUtil.toSong(pt.track, af)
//        })
//      api.getFutureList[Song](f)
//    }
//  }

//  private def getSong(href: String): Future[Song] = {
//    tracksApi.getPlaylistTracks(href) flatMap { page =>
//      val playlist = page.items.head
//      tracksApi.getAudioFeatures(page.items.head.track.id.get) map {
//        af => MusicUtil.toSong(page.items.head.track, af)
//      }
//    }
//  }

  /*
  private def songs(href: String): Future[List[Song]] = {
    for {
      tracks: List[Track] <- tracksApi.getPlaylistTracks(href) map { p => p.items map { pt => pt.track } }
      af: List[AudioFeatures] <- tracks.map(t => tracksApi.getAudioFeatures(t.id.get))
    } yield MusicUtil.toSongs(tracks.zip(af))
  }
  */

  /*
  def songs(href: String): Action[AnyContent] = handleAsync {
    tracksApi.getPlaylistTracks(href) map {
      t => {
        val f: String = for {
          tracks <- t.items.map(pt => pt.track)
          af <- tracksApi.getAudioFeatures(tracks.id.get)
        } yield (tracks, af)
        Ok(views.html.tracks("Some playlist", ("", f) :: Nil))
      }
    }
  }
  */

  def sampleTracks: Action[AnyContent] = handleAsync {
    Future(Ok(views.html.exception("TODO - GET SAMPLE TRACKS FROM DB")))
  }

  // def newReleases: Future[List[SimpleAlbum]] = spotify.browse.newReleases

  private def handleAsync(block: => Future[Result]): Action[AnyContent] = Action.async {
    try block
    catch {
      case auth: WebApiException => Future(BadRequest(views.html.exception(auth.getMessage)))
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
