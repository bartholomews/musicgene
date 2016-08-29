package controllers

import javax.inject.{Inject, Singleton}

import com.mongodb.ServerAddress
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI, MongoCredential}
import com.wrapper.spotify.exceptions.BadRequestException
import model.music.{Cache, Song}
import play.api.Play
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.{MongoConnection, MongoConnectionOptions, MongoDriver, ScramSha1Authentication}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.core.nodeset.Authenticate

import collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import com.mongodb.casbah.Imports._

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
class HomeController @Inject()(implicit exec: ExecutionContext, config: play.api.Configuration) extends Controller {

  val doc = BSONDocument(
    "firstName" -> "Stephane",
    "lastName" -> "Godbillon",
    "age" -> 29)

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
      //playlists.foreach(v => writeSongsToJSON(v._2))
      writeToDB(Vector())
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

  def writeToDB(songs: Vector[Song]) = {

    val db = getMongoDB

    val collection = db("tracks")
    val allDocs = collection.find()
    println("FIND()")
    println(allDocs)
    for(doc <- allDocs) println(doc)
    val n = collection.count()
    println("COUNT: " + n)
    /*
    val a = MongoDBObject("hello" -> "world")
    collection.insert( a )
    println("INSERT()")
    */
  }

  private def getMongoDB: MongoDB = {
    val name = "admin"
    val host = config.underlying.getString("mongodb.host")
    val port = config.underlying.getInt("mongodb.port")
    val dbName = config.underlying.getString("mongodb.db")
    val server = new ServerAddress(host, port)
    val credentials = MongoCredential.createScramSha1Credential(name, dbName, name.toCharArray)
    val mongoClient = MongoClient(server, List(credentials))
    mongoClient(dbName)
  }

}