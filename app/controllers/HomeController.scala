package controllers

import javax.inject._

import com.wrapper.spotify.exceptions.BadRequestException
import com.wrapper.spotify.models.{AudioFeature, SimplePlaylist, Track}
import model.music.{Attribute, MusicCollection, MusicUtil, Song}
//import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc._

import scala.collection.JavaConversions._
import collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {
  val spotify = SpotifyController.getInstance()

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

  // pass just the minimum necessary, why all the weight, unused.
  def getTracks = Action {
    try {
      // not sure about this. How's caching? Async?
    //  val tracks = spotify.getSavedTracks(0, 50).toList.map(t => t.getTrack)

      // vector might be better than list in this case, see odersky
      val simplePlaylists: Vector[SimplePlaylist] = spotify.getSavedPlaylists.toVector
      val collection: Vector[(SimplePlaylist, Vector[(Track, AudioFeature)])] = getPlaylistsCollection(simplePlaylists)

      // TODO refactor to be a List or Vector of Playlist(string name as val inside)
      val playlists: Vector[(String, Vector[Song])] = collection.map {
        c => (c._1.getName, MusicUtil.toSongs(c._2))
      }

//TODO      writeSongsToJSON(new MusicCollection(MusicUtil.toSongs(collection.flatMap(p => p._2))))


      Ok(views.html.tracks("PLAYLISTS", playlists))

    } catch {
      case _:BadRequestException => BadRequest("That was a bad request.") // TODO implement Button BACK to index
      case _:NullPointerException => BadRequest("Something went wrong.") // should return something else not badreq>
      // case _  => // some other exception handling
    }
  }

  def getPlaylistsCollection(list: Vector[SimplePlaylist]): Vector[(SimplePlaylist, Vector[(Track, AudioFeature)])] = {
    val tuple: Vector[(SimplePlaylist, Vector[Track])] = getPlaylistTuple(list)
    tuple.map(t => (t._1, getTracksFeatures(t._2)))
  }

  /*
  def getPlaylistsCollection(list: List[SimplePlaylist]): List[(SimplePlaylist, List[Song])] = {
    val tuple: List[(SimplePlaylist, List[Track])] = getPlaylistTuple(list)
    tuple.map(t => (t._1, getTracksFeatures(t._2)))
  }
  */

  def getPlaylistCollection(playlist: SimplePlaylist): (SimplePlaylist, Seq[Song]) = {
    val trackList = spotify.getPlaylistTracks(playlist).map(t => t.getTrack).toVector
    val songs = MusicUtil.toSongs(trackList.map(t => (t, spotify.getAnalysis(t.getId))))
    (playlist, songs)
  }

  /*
  def writePlaylistsToJSON(db: List[(SimplePlaylist, List[Song])] = {
    db.foreach(p => JsonController.writeJSON(p._1, p._2.map(s => s.)))
  }
  */

  def writeSongsToJSON(db: MusicCollection) = {
    db.songs.foreach(s => JsonController.writeJSON(s.id, s.attributes.asJava))
  }

  def getTracksFeatures(list: Vector[Track]): Vector[(Track, AudioFeature)] = {
    list.map { t => (t, spotify.getAnalysis(t.getId)) }
  }

  // TODO async and multibucket
  def createCollection(list: Vector[Track]): MusicCollection = {
    val tracks: Vector[(Track, AudioFeature)] = list.map { t => (t, spotify.getAnalysis(t.getId)) }
    new MusicCollection(MusicUtil.toSongs(tracks))
  }

  def getPlaylistTracks(playlist: SimplePlaylist): Vector[Track] = {
    val debug = spotify.getPlaylistTracks(playlist).map(t => t.getTrack).toVector
    debug.foreach(t => println(t.getName))
    debug
  }

  def getPlaylistTuple(playlists: Vector[SimplePlaylist]): Vector[(SimplePlaylist, Vector[Track])] = {
    playlists.map(p => (p, spotify.getPlaylistTracks(p).map(t => t.getTrack).toVector))
  }

  // this makes sense and you should modify the wrapper to do multiple requests at one time
  def getIDs(tracks: List[Track]): String = tracks.map(t => t.getId).mkString(",")

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
