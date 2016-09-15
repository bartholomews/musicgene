package controllers

import javax.inject.{Inject, Singleton}

import com.wrapper.spotify.exceptions.BadRequestException
import model.music.Song
import play.api.mvc.{Action, Controller}

@Singleton
class HomeController @Inject() extends Controller {

  def index = Action {
    Ok(views.html.index("GEN"))
  }

  def getSampleTracks = Action {
    // retrieve 200 sample tracks from MongoDB
    val songs = MongoController.read(200)
    Ok(views.html.tracks("sample songs", Vector(("A list of unsorted tracks with different characteristics",
      songs))))
  }

  def getSpotifyTracks = Action {
    try {
      val spotify = new SpotifyController
      val userName = spotify.getSpotifyName
      val playlists: Vector[(String, Vector[Song])] = spotify.getPlaylists
      Ok(views.html.tracks(userName, playlists))
    } catch {
      // @see https://developer.spotify.com/web-api/user-guide/
      case x: BadRequestException => {
        x.printStackTrace()
        BadRequest("That was a bad request.") // 429 falls here
      } // TODO implement Button BACK to index
      case _: NullPointerException => BadRequest("Something went wrong.")
    }
  }

}