package controllers

import javax.inject.{Inject, Singleton}

import com.wrapper.spotify.exceptions.BadRequestException
import controllers.wrapper.SpotifyAPI
import logging.AccessLogging
import controllers.wrapper.entities.{Page, SimpleAlbum, SimplePlaylist, Track}
import model.music.Song
import play.api.Logger
import play.api.cache.redis.CacheApi
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Main controller to retrieve the music collection
  * either from the database or from the Spotify API via `SpotifyController`
  *
  * @param configuration the MongoDB server configuration injected from .conf file when the application starts
  */
@Singleton
class HomeController @Inject()(configuration: play.api.Configuration, cache: CacheApi, spotify: SpotifyAPI) extends Controller with AccessLogging {

  val logger = Logger("application")
  /**
    * Redirect a user to authenticate with Spotify and grant permissions to the application
    *
    * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
    */
  def auth = Action { Redirect(spotify.api.authoriseURL) }

  def callback = Action.async {
    request => request.getQueryString("code") match {
      case Some(code) => try {
        spotify.api.withAuthToken(Some(code)) { token => hello }
      } catch { case e: Exception => handleException(e) }
      case None => request.getQueryString("error") match {
        case Some("access_denied") => Future(BadRequest("You need to authorize permissions in order to use the App."))
        case Some(error) => Future(BadRequest(error))
        case _ => Future(BadRequest("Something went wrong."))
      }
    }
  }

  def handleException(e: Exception): Future[Result] = {
    Future(Ok(e.getMessage))
  }

  def get = Action.async { Future(Ok("OK")) }

  def playlists = spotify.playlists.myPlaylists map {
    p => Ok(p.items.toString)
  }

  def newReleases: Future[List[SimpleAlbum]] = spotify.browse.newReleases

  def playlistsAction = Action.async { playlists }

  //def allMyPlaylists: Future[List[SimplePlaylist]] = spotify.playlists.myPlaylists // TODO

  /*
  def allMyPlaylistsTracks: Future[List[Page[Track]]] = allMyPlaylists flatMap {
    list: List[SimplePlaylist] => spotify.api.getList[Page[Track]](list.map(p => p.tracks.href))
  }
  */

  /*
  def allPlaylistsAction = Action.async {
    allMyPlaylistsTracks map {
      p =>
        val titles: List[String] = p.flatMap(pg => pg.items.map(t => t.name))
        Ok(titles.mkString(", "))
    }
  }
  */

  def browse = spotify.browse.featuredPlaylists map {
    fp => Ok(fp.message)
  }

  def hello = spotify.profiles.me map {
    me => Ok(views.html.callback(s"Hello, ${me.id}"))
  }

  /**
    * The 'tracks' collection at injected MongoDB server
    */
  val dbTracks = MongoController.getCollection(
    configuration.underlying.getString("mongodb.uri"),
    configuration.underlying.getString("mongodb.db"),
    configuration.underlying.getString("mongodb.tracks")
  )

  /**
    * @return an HTTP Ok (Status 200) rendering index.scala.html in views package
    */
  /*
  play.api.mvc.Action type is a wrapper
  around the type `Request[A] => Result`,
  where `Request[A]` identifies an HTTP request
  and `Result` is an HTTP response.
  */
  def index = Action {
    Ok(views.html.index("musicgene"))
  }

  /**
    * Retrieve the first 200 tracks stored in the MongoDB database,
    * and return them rendering the view tracks.scala.html
    *
    * @return an HTTP Ok 200 on tracks view with 200 Song instances retrieved from MongoDB
    */
  def getSampleTracks = Action {
    // retrieve 200 sample tracks from MongoDB
    val songs = MongoController.read(dbTracks, 200)
    Ok(views.html.tracks("sample",
      Vector(("A list of unsorted tracks with different characteristics", songs)))
    )
  }

  /**
    * Retrieve a user's playlists tracks from the Spotify account via SpotifyController
    * (which will access the Spotify API if a track is not already
    * stored in MongoDB)
    *
    * @return an HTTP Ok 200 on tracks view
    *         with the playlist tracks belonging to the Spotify user account
    *         or a HTTP Bad Request 400 if a problem occurred
    */
  def getSpotifyTracks = Action {
    try {
      val spotify = new SpotifyController(configuration, cache)
      val userName = spotify.getSpotifyName
      val playlists: Vector[(String, Vector[Song])] = spotify.getPlaylists
      Ok(views.html.tracks(userName, playlists))
    } catch {
      // @see https://developer.spotify.com/web-api/user-guide/
      case x: BadRequestException => {
        x.printStackTrace()
        BadRequest("That was a bad request.") // 429: too many requests from Spotify falls here
      }
      case _: NullPointerException => BadRequest("Something went wrong.")
    }
  }

}