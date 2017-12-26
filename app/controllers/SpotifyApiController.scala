package controllers

import utils.FutureUtils
import com.google.inject.Inject
import com.mongodb.casbah.Imports
import it.turingtest.spotify.scala.client.logging.AccessLogging
import it.turingtest.spotify.scala.client._
import it.turingtest.spotify.scala.client.entities._
import model.music.{MusicUtil, Song}
import play.api.cache
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  */
class SpotifyApiController @Inject()(configuration: play.api.Configuration,
                                     api: BaseApi,
                                     authApi: AuthApi,
                                     playlistsApi: PlaylistsApi,
                                     profilesApi: ProfilesApi,
                                     tracksApi: TracksApi) extends Controller with AccessLogging {


  /**
    * The 'tracks' collection at injected MongoDB server
    */
  val dbTracks: Imports.MongoCollection = MongoController.getCollection(
    configuration.underlying.getString("mongodb.uri"),
    configuration.underlying.getString("mongodb.db"),
    configuration.underlying.getString("mongodb.tracks")
  )

  /**
    * Redirect a user to authenticate with Spotify and grant permissions to the application
    *
    * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
    */
  def auth = Action {
    Redirect(authApi.authoriseURL(state = None, scopes = List(), showDialog = false))
  }

  /**
    * TODO handleException(e) { _ }
    *
    * @return
    */
  def callback: Action[AnyContent] = Action.async {
    request =>
      request.getQueryString("code") match {
        case Some(code) => handleError(api.setAuthAndThen(code) { _ => hello() })
        case None => request.getQueryString("error") match {
          case Some("access_denied") => Future(BadRequest("You need to authorize permissions in order to use the App."))
          case Some(error) => Future(BadRequest(error))
          case _ => Future(BadRequest("Something went wrong."))
        }
      }
  }

  private def handleAsync(block: => Future[Result]): Action[AnyContent] = Action.async { handleError(block) }

  /**
    * TODO if e.getMessage is authorization_code_not_provided, you should be able to re-login automatically
    * especially if no_dialog was set to false, anyway even if was set to true could send the user straight there
    * instead of showing the error, or at least showing this error with a link and caching this request to get back
    * as soon as the user has logged in again;
    * @param f
    * @tparam T
    * @return
    */
  private def handleError[T](f: Future[T]): Future[T] = {
    f onFailure {
      case auth: WebApiException => Future(BadRequest(views.html.exception(auth.getMessage)))
      case ex: Exception =>
        accessLogger.debug(ex.getMessage)
        BadRequest(s"There was a problem loading this page. Please try again.\n${ex.getMessage}")
    }
    f
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

  def playlistTracks(): Action[AnyContent] = handleAsync {
    profilesApi.myPlaylists flatMap { p =>
      playlistsApi.playlist(p.items.head.owner.id, p.items.head.id) map {
        p => Ok(views.html.playlistTracks(p.name, List()))
      }
    }
  }

  def songs(user_id: String, playlist_id: String): Action[AnyContent] = handleAsync {
    playlistsApi.playlist(user_id, playlist_id) flatMap { p =>
      val f: Seq[Future[Song]] = p.tracks.items.map(pt =>
        pt.track.id match {

          case Some(id) =>
            tracksApi.getAudioFeatures(id) map { af =>
              val song = MusicUtil.toSong(pt.track, af)
              MongoController.writeToDB(dbTracks, song) // TODO only if not already there
              song
            }
          case None => Future(MusicUtil.toSong(pt.track))

        })

      FutureUtils.collectFutures(f) map {
        s => Ok(views.html.tracks(s"${p.name}'s tracks", List((p.name, s))))
      }
    }
  }

  /**
    * Retrieve the first 200 tracks stored in the MongoDB database,
    * and return them rendering the view tracks.scala.html
    *
    * @return an HTTP Ok 200 on tracks view with 200 Song instances retrieved from MongoDB
    */
  def sampleTracks = Action {
    // retrieve 200 sample tracks from MongoDB
    val songs = MongoController.read(dbTracks, 200)
    Ok(views.html.tracks("sample",
      Vector(("A list of unsorted tracks with different characteristics", songs)))
    )
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

  // def newReleases: Future[List[SimpleAlbum]] = spotify.browse.newReleases

  /**
    * Retrieve a playlist's underlying tracks' audio attributes.
    *
    * @return a SimplePlaylist with associated sequence of Song instances
    *         wrapped in an Option, if an error occurred None is returned
    */
//  def getPlaylistCollection(userId: String, playlistId: String): Option[(SimplePlaylist, Vector[Song])] = {
//    try {
//      // get each playlist's tracks (only ids and basic access data)
//      val trackList: Future[Page[PlaylistTrack]] = playlistsApi.tracks(userId, playlistId)
//      // retrieve those which are already stored in Redis cache
//      val inCache: Vector[Song] = trackList.flatMap(t => cache.get[Song](t.))
//      // tracks not stored in cache but stored in MongoDB
//      val inDB: Vector[Song] = trackList.filterNot(t => inCache.exists(s => s.id == t.getId))
//        .flatMap(t => MongoController.readByID(dbTracks, t.getId))
//      inDB.foreach(s => cache.set(s.id, s))
//      val retrieved = inCache ++ inDB
//      // create a sequence of Song with those retrieved from MongoDB
//      // and getting the others from the Spotify API
//      val outDB: Vector[Song] = MusicUtil.toSongs(
//        trackList.filterNot(t => retrieved.exists(s => s.id == t.getId))
//          .flatMap(t => spotify.getAnalysis(t.getId) match {
//            case None => None
//            case Some(analysis) => Some(t, analysis)
//          }
//          ))
//      outDB.foreach(s => {
//        // write to MongoDB those Song instances which weren't there
//        MongoController.writeToDB(dbTracks, outDB)
//        // write to Redis cache
//        cache.set(s.id, s)
//      })
//      // return the playlist and its tracks
//      Some(playlist, retrieved ++ outDB)
//    } catch {
//      // return None if an error occurred during the operation
//      case _: NullPointerException => None
//      case _: BadRequestException => None
//    }
//  }


}
