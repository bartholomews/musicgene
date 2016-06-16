package controllers

import javax.inject._

import com.wrapper.spotify.exceptions.BadRequestException
import com.wrapper.spotify.models.Track
import model.music.MusicCollection
import play.api._
import play.api.mvc._

import scala.collection.JavaConversions._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {
  val spotify = SpotifyController.getInstance()

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("M-I-R"))
  }

  def getTracks = Action {
    try {
      // not sure about this. How's caching?
      val tracks: List[Track] = spotify.getSavedTracks.toList.map(t => t.getTrack)
      val collection = new MusicCollection(tracks) // TODO async
      println(collection.toString)
      Ok(views.html.tracks("TRACKS!", tracks))
    } catch {
      case _:BadRequestException => BadRequest("That was a bad request.")
      case _:NullPointerException => BadRequest("Something went wrong.") // should return something else not badreq>
      // case _  => // some other exception handling
    }
  }

}
