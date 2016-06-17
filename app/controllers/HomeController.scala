package controllers

import javax.inject._

import com.wrapper.spotify.exceptions.BadRequestException
import com.wrapper.spotify.models.{AudioFeature, Track}
import model.music.MusicCollection
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc._

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (ws: WSClient) extends Controller {
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
  //    val tracks: List[(Track, AudioFeature)] = spotify.getSavedTracks.toList.map
  //    { t => (t.getTrack, getTracksAnalysis(t.getTrack)) }
      val tracks: List[Track] = spotify.getSavedTracks.toList.map(t => t.getTrack)
      val collection = new MusicCollection(tracks) // TODO async
      println(collection.toString)
      val analysis = getTracksAnalysis(tracks)
      analysis.foreach(a => println("TEMPO: " + a.getTempo))
      Ok(views.html.index("FOCKOFF"))
    } catch {
      case _:BadRequestException => BadRequest("That was a bad request.")
      case _:NullPointerException => BadRequest("Something went wrong.") // should return something else not badreq>
      // case _  => // some other exception handling
    }
  }

  // this would make sense and you should modify the wrapper to do multiple requests at one time
  def getIDs(tracks: List[Track]): String = tracks.map(t => t.getId).mkString(",")

  def getTracksAnalysis(tracks: List[Track]): List[AudioFeature] = {
    tracks.map(t => spotify.getAnalysis(t.getId))
  }

  /*
  def getTracksAnalysis(tracks: List[Track]) = {
    val code = "BQAHIwOOnWpUS_amq7ZSjgHglMdCOikKmmRnLrJJeECaLFWHvUeqvR82j7p17kc5m8ClU6ekGgJiOybgLd7vsH5fH8"
    val wsURL = ws.url("https://api.spotify.com/v1/audio-features")
    val ids = tracks.map(t => t.getId).mkString(",")
    val bear = "Bearer " + code

    val req: WSRequest =
      wsURL.withHeaders("Authorization" -> bear)
        .withQueryString("ids" -> ids)

    println(req.toString)

    val result = req.get().map {
      //  response => (response.json \ "danceability").as[String]re
      response => response.allHeaders.toString()
    }
    */

}
