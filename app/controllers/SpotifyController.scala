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
    /**
      * TODO
      * is badRequest a proper return code for each?
      * if VALUES: [access_denied] return badRequest(index.render(...));
      * else (if VALUES: [invalid_scope] or something else)
      * return badRequest(callback.render(map.get("error")[0]));
      */
  }

  def getSpotifyName: String = spotify.getName

  def getPlaylists: Vector[(String, Vector[Song])] = {
    val simplePlaylists: Vector[SimplePlaylist] = spotify.getSavedPlaylists.toVector
    System.out.println("Playlists retrieved")
    val playlists: Vector[(SimplePlaylist, Vector[Song])] = getPlaylistsCollection(simplePlaylists)
    playlists.map(v => (v._1.getName, v._2))
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
