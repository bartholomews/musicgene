package controllers

import javax.inject.{Inject, Singleton}

import com.wrapper.spotify.exceptions.BadRequestException
import model.music.Song
import play.api.cache.redis.CacheApi
import play.api.mvc.{Action, Controller}

/**
  * Main controller to retrieve the music collection
  * either from the database or from the Spotify API via `SpotifyController`
  *
  * @param configuration the MongoDB server configuration injected from .conf file when the application starts
  */
@Singleton
class HomeController @Inject()(configuration: play.api.Configuration, cache: CacheApi) extends Controller {

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
    Ok(views.html.tracks("sample songs",
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