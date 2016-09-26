package controllers

import javax.inject._

import com.wrapper.spotify.exceptions.BadRequestException
import com.wrapper.spotify.models.{SimplePlaylist, Track}
import model.music._
import play.api.cache.redis.CacheApi
import play.api.mvc._

import scala.collection.JavaConversions._

/**
  * Controller which redirects the user to authenticate with Spotify,
  * then retrieves the tracks from the API if not already in MongoDB
  *
  * @param configuration the MongoDB server configuration injected from .conf file when the application starts
  */
@Singleton
class SpotifyController @Inject()(configuration: play.api.Configuration, cache: CacheApi) extends Controller {
  // Singleton instance of the SpotifyJavaController which interface with the Spotify API Java wrapper
  val spotify = SpotifyJavaController.getInstance()

  /**
    * The 'tracks' collection at injected MongoDB server
    */
  val dbTracks = MongoController.getCollection(
    configuration.underlying.getString("mongodb.uri"),
    configuration.underlying.getString("mongodb.db"),
    configuration.underlying.getString("mongodb.tracks")
  )

  /**
    * Redirect a user to authenticate with Spotify and grant permissions to the application
    *
    * @return a Redirect Action via SpotifyJavaController
    *         (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
    */
  def auth = Action { Redirect(spotify.getAuthorizeURL) }


  /**
    * Retrieve the authorization code from the user request,
    * and use it in SpotifyJavaController to get an Access Token.
    *
    * @return an HTTP 200 Ok rendering callback view
    *         if the user is successfully authenticated,
    *         a 400 Bad Request otherwise
    */
  def callback = Action {
    request => request.getQueryString("code") match {
      case Some(code) =>
        spotify.getAccessToken(code)
        Ok(views.html.callback("Welcome, " + spotify.getName))
      case None => BadRequest(views.html.index("musicgene")) // BadRequest("Something went wrong. Please go back and try again.")
    }
  }

  /**
    * @return the user Spotify account name
    */
  def getSpotifyName: String = spotify.getName

  /**
    * @return the user's saved playlists as a Vector wrapping
    *         each Playlist (String name and Vector[Song])
    */
  def getPlaylists: Vector[(String, Vector[Song])] = {
    // user playlists returned as SimplePlaylist instances by the Java wrapper
    val simplePlaylists: Vector[SimplePlaylist] = spotify.getSavedPlaylists.toVector
    // each will be mapped to associate a sequence of Song instances per Playlist
    // (i.e. resolving track ids into Song with audio attributes)
    val playlists: Vector[(SimplePlaylist, Vector[Song])] = getPlaylistsCollection(simplePlaylists)
    // retain only the playlist name from SimplePlaylist, and the sequence of Song instances
    playlists.map(v => (v._1.getName, v._2))
  }

  /**
    * Map a SimplePlaylist to retrieve its underlying tracks' audio attributes.
    * The result is flattened in order to discard failing operations (i.e. returned as None)
    *
    * @param list the playlists to operate on
    * @return a sequence of SimplePlaylists with associated sequences of Song instances
    */
  def getPlaylistsCollection(list: Vector[SimplePlaylist]): Vector[(SimplePlaylist, Vector[Song])] = {
    list.flatMap(p => getPlaylistCollection(p))
  }

  /**
    * Retrieve a playlist's underlying tracks' audio attributes.
    *
    * @param playlist the playlist to operate on
    * @return a SimplePlaylist with associated sequence of Song instances
    *         wrapped in an Option, if an error occurred None is returned
    */
  def getPlaylistCollection(playlist: SimplePlaylist): Option[(SimplePlaylist, Vector[Song])] = {
    try {
      // get each playlist's tracks (only ids and basic access data)
      val trackList: Vector[Track] = spotify.getPlaylistTracks(playlist).map(t => t.getTrack).toVector

      // retrieve those which are already stored in MongoDB
      // val inDB: Vector[Song] = trackList.flatMap(t => MongoController.readByID(dbTracks, t.getId))

      // retrieve those which are already stored in Redis cache
      val inCache: Vector[Song] = trackList.flatMap(t => cache.get[Song](t.getId))
      // tracks not stored in cache but stored in MongoDB
      val inDB: Vector[Song] = trackList.filterNot(t => inCache.exists(s => s.id == t.getId))
                          .flatMap(t => MongoController.readByID(dbTracks, t.getId))
      inDB.foreach(s => cache.set(s.id, s))
      val retrieved = inCache ++ inDB
      // create a sequence of Song with those retrieved from MongoDB
      // and getting the others from the Spotify API
      val outDB: Vector[Song] = MusicUtil.toSongs(
        trackList.filterNot(t => retrieved.exists(s => s.id == t.getId))
          .flatMap(t => spotify.getAnalysis(t.getId) match {
            case None => None
            case Some(analysis) => Some(t, analysis)
          }
      ))

      outDB.foreach(s => {
        // write to MongoDB those Song instances which weren't there
        MongoController.writeToDB(dbTracks, outDB)
        // write to Redis cache
        cache.set(s.id, s)
      })
      // return the playlist and its tracks
      Some(playlist, retrieved ++ outDB)
    } catch {
      // return None if an error occurred during the operation
      case _: NullPointerException => None
      case _: BadRequestException => None
    }
  }

}
