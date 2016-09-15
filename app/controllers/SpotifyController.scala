package controllers

import javax.inject._

import com.wrapper.spotify.exceptions.BadRequestException
import com.wrapper.spotify.models.{SimplePlaylist, Track}
import model.music._
import play.api.mvc._

import scala.collection.JavaConversions._

/**
 * This controller redirects the user to authenticate with Spotify,
  * then retrieves the tracks from the API if not already in MongoDB
  *
 */
@Singleton
class SpotifyController @Inject() extends Controller {
  val spotify = SpotifyJavaController.getInstance()

  def auth = Action { Redirect(spotify.getAuthorizeURL) }

  /*
  play.api.mvc.Action type is a wrapper
  around the type `Request[A] => Result`,
  where `Request[A]` identifies an HTTP request
  and `Result` is an HTTP response.
  */
  def callback = Action {
    request => request.getQueryString("code") match {
      case Some(code) =>
        spotify.getAccessToken(code)
        Ok(views.html.callback("Welcome, " + spotify.getName))
      case None => BadRequest("Something went wrong. Please go back and try again.")
    }
  }

  def getSpotifyName: String = spotify.getName

  def getPlaylists: Vector[(String, Vector[Song])] = {
    val simplePlaylists: Vector[SimplePlaylist] = spotify.getSavedPlaylists.toVector
    val playlists: Vector[(SimplePlaylist, Vector[Song])] = getPlaylistsCollection(simplePlaylists)
    playlists.map(v => (v._1.getName, v._2))
  }

  def getPlaylistsCollection(list: Vector[SimplePlaylist]): Vector[(SimplePlaylist, Vector[Song])] = {
    list.flatMap(p => getPlaylistCollection(p))
  }

  def getPlaylistCollection(playlist: SimplePlaylist): Option[(SimplePlaylist, Vector[Song])] = {
    try {
      val trackList: Vector[Track] = spotify.getPlaylistTracks(playlist).map(t => t.getTrack).toVector
      val inDB: Vector[Song] = trackList.flatMap(t => MongoController.readByID(t.getId))
      val outDB: Vector[Song] = MusicUtil.toSongs(
        trackList.filterNot(t => inDB.exists(s => s.id == t.getId))
          .flatMap(t => spotify.getAnalysis(t.getId) match {
            case None => None
            case Some(analysis) => Some(t, analysis)
          }
      ))
      MongoController.writeToDB(outDB)
      Some(playlist, inDB ++ outDB)
    } catch {
      case _: NullPointerException => None
      case _: BadRequestException => None
    }
  }

  def getPlaylistTracks(playlist: SimplePlaylist): Vector[Track] = {
    val debug = spotify.getPlaylistTracks(playlist).map(t => t.getTrack).toVector
    debug.foreach(t => println(t.getName))
    debug
  }

  def getPlaylistTuple(playlists: Vector[SimplePlaylist]): Vector[(SimplePlaylist, Vector[Track])] = {
    playlists.map(p => (p, spotify.getPlaylistTracks(p).map(t => t.getTrack).toVector))
  }

  def getIDs(tracks: List[Track]): String = tracks.map(t => t.getId).mkString(",")

}
