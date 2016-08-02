package controllers

import javax.inject._

import com.wrapper.spotify.exceptions.BadRequestException
import com.wrapper.spotify.models.{AudioFeature, SimplePlaylist, Track}
import model.music._
//import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc._

import scala.collection.JavaConversions._
import collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
  *
  * TODO should retrieve all IDs single call,
  * if not in the cache then retrieve the audioAnalysis
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
      System.out.println("Playlists retrieved")
      val playlists: Vector[(SimplePlaylist, Vector[Song])] = getPlaylistsCollection(simplePlaylists)
      System.out.println("Collection retrieved")
      // TODO refactor to be a List or Vector of Playlist(string name as val inside)

      playlists.foreach(v => writeSongsToJSON(v._2))

      System.out.println("Ready to view playlists...")

      Ok(views.html.tracks("PLAYLISTS", playlists.map(v => (v._1.getName, v._2))))

    } catch {
      // @see https://developer.spotify.com/web-api/user-guide/
      case _:BadRequestException => {
        BadRequest("That was a bad request.")
      } // TODO implement Button BACK to index
      case _:NullPointerException => BadRequest("Something went wrong.") // should return something else not badreq>
      // case _  => // some other exception handling
    }
  }

  def getPlaylistsCollection(list: Vector[SimplePlaylist]): Vector[(SimplePlaylist, Vector[Song])] = {
    list.map(p => getPlaylistCollection(p))
    /*
    val tuple: Vector[(SimplePlaylist, Vector[Track])] = getPlaylistTuple(list)
    tuple.map(t => (t._1, getTracksFeatures(t._2)))
  */
  }

  /*
  def getPlaylistsCollection(list: List[SimplePlaylist]): List[(SimplePlaylist, List[Song])] = {
    val tuple: List[(SimplePlaylist, List[Track])] = getPlaylistTuple(list)
    tuple.map(t => (t._1, getTracksFeatures(t._2)))
  }
  */

  // TODO rate limits...
  def getPlaylistCollection(playlist: SimplePlaylist): (SimplePlaylist, Vector[Song]) = {
    println("creating playlist collection..")
    val trackList: Vector[Track] = spotify.getPlaylistTracks(playlist).map(t => t.getTrack).toVector
    val (inCache, outCache) = Cache.getFromCache(trackList.map(t => t.getId))
    (playlist, inCache ++ MusicUtil.toSongs(
      trackList.filter(t => outCache.contains(t.getId))
                .map { t => (t, spotify.getAnalysis(t.getId)) }
    ))
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
    songs.foreach(s => JsonController.writeJSON(s.id, s.preview_url, s.attributes.asJava))
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
