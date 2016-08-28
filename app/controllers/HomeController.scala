package controllers

import javax.inject.{Inject, Singleton}

import com.wrapper.spotify.exceptions.BadRequestException
import model.music.{Cache, Song}
import play.api.mvc.{Action, Controller}

import collection.JavaConverters._

/**
  *
  */
/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  *
  * TODO should retrieve all IDs single call,
  * if not in the cache then retrieve the audioAnalysis
  */
@Singleton
class HomeController @Inject() extends Controller {

  /**
    * TODO THE WHOLE PROCESS OF RETRIEVING DATA SHOULD GO TO ITN OWN CLASS
    * I NEED MUSIC_UTIL.GetSONG(id) as well (now in Cache)
    *
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = Action {
    Ok(views.html.index("GEN"))
  }

  def getSampleTracks = Action {
    val songs = Cache.extractSongs
    // TODO inject
    Ok(views.html.tracks("SAMPLE SONGS", Vector(("A list of unsorted tracks with different characteristics",
      songs))))
  }

  def getUserPlaylists = Action {
    try {
      val spotify = new SpotifyController
      val userName = spotify.getSpotifyName
      val playlists = spotify.getPlaylists
      playlists.foreach(v => writeSongsToJSON(v._2))
      Ok(views.html.tracks(userName, playlists))
    } catch {
      // @see https://developer.spotify.com/web-api/user-guide/
      case _: BadRequestException => {
        BadRequest("That was a bad request.")
      } // TODO implement Button BACK to index
      case _: NullPointerException => BadRequest("Something went wrong.") // should return something else not badreq>
      // case _  => // some other exception handling
      // case 429 (too many requests) : maybe should be catched in SpotifyController
    }
  }

  /*
//TODO
def writePlaylistsToJSON(db: List[(SimplePlaylist, List[Song])]) = {
  db.foreach(p => {
    p._2.foreach(s => JsonController.writeJSON(s.id, s.preview_url, s.attributes))
  })
}
*/

  def writeSongsToJSON(songs: Vector[Song]) = {
    songs.foreach(s => JsonController.writeJSON(s.id, s.attributes.asJava))
  }

}
