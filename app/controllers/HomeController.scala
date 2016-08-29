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
class HomeController @Inject()(implicit exec: ExecutionContext, config: play.api.Configuration, val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  val uri = config.underlying.getString("mongodb.uri")

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
      writeSongsToJSON(Vector())
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

  /*CASBAH FAIL
  def writeSongsToJSON(songs: Vector[Song]) = {
  //  val uri = MongoClientURI(configuration.underlying.getString("mongodb.uri"))
  val uri = MongoClientURI (
    "mongodb://heroku_pzmhfhvt:a6b0qlv64dmc19pghfuipejdcf@ds017896.mlab.com:17896/heroku_pzmhfhvt?authMode=scram-sha1"
  )
    val mongoClient =  MongoClient(uri)
    val db = mongoClient("heroku_pzmhfhvt")

    val collection = db("tracks")
    println("COLLECTION RETRIEVED: ")
    val c: Int =  collection.count()
    println("COLLECTION HAS " + c + " ELEMENTS")
    val a = MongoDBObject("hello" -> "world")
    println("1")
    collection.insert(a)
    println("2")
    val allDocs = collection.find()
    println( allDocs )
    for(doc <- allDocs) println( doc )
    println("THE_END")
  }

}
*/
  def connect(driver: MongoDriver): Try[MongoConnection] = {
    println("ESTABLISHING CONNECTION...")
    MongoConnection.parseURI(uri).map { parsedURI =>
      driver.connection(parsedURI)
    }
  }

  def connect2(): MongoConnection = {
    def server1 = List("ds017896.mlab.com:17896")
    val dbName = "heroku_pzmhfhvt"
    val userName = "heroku_pzmhfhvt"
    val password = "a6b0qlv64dmc19pghfuipejdcf"
    val connectionOptions = MongoConnectionOptions(authMode = ScramSha1Authentication)
    val credentials = List(Authenticate(dbName, userName, password))
    val driver1 = new MongoDriver
    driver1.connection(server1, authentications = credentials, options = connectionOptions)
  }

  def writeSongsToJSON(songs: Vector[Song]) = {

    val a = MongoDBObject("hello" -> "world")
    val userName = "bartholomews"
    val dbName = "heroku_pzmhfhvt"
    val password = "Sp4c3M0nk3y"
    /*
    val yURI = MongoClientURI(uri)
    val mongoClient = MongoClient(yURI)
    val db = mongoClient("heroku_pzmhfhvt")
    */

    val server = new ServerAddress("ds017896.mlab.com", 17896)
    //val server = new ServerAddress("localhost", 27017)

  val credentials = MongoCredential.createScramSha1Credential(userName, dbName, password.toCharArray)
  val mongoClient = MongoClient(server, List(credentials))

    val db = mongoClient(dbName)
    //val db = mongoClient("db1")
    val collection = db("tracks")
    val allDocs = collection.find()
    println("FIND()")
    println(allDocs)
    for(doc <- allDocs) println(doc)
    val n = collection.count()
    println("COUNT: " + n)
    collection.insert( a )
    println("INSERT()")


    /*
    val connect: MongoConnection = connect1().get
    val dbName = "heroku_pzmhfhvt"
    val userName = "heroku_pzmhfhvt"
    val pwd = "a6b0qlv64dmc19pghfuipejdcf"

    val ahaha = connect.authenticate(db = dbName, user = userName, password = pwd)
    ahaha.onComplete {
      case Failure(e) => println("ANOTHER INCREDIBLE FAIL! =======> " + e)
      case Success(c) => println("WOW! THAT IS REALLY A GREAT SUCCESS! =======> ")
    }

    println("GOT COLLECTION")
    get(connect)
    println("FUCK! FUCK! ")
    get(connect)
    println("FUCKFUCKFUCK!")
    */
  }

  def get(connection: MongoConnection) = {
    val future = connection.database("heroku_pzmhfhvt").map(_.collection("track"))
    future.onComplete {
      case Failure(e) => {
        println("FAIL! =======> " + e)
        None
      }
      case Success(c) => {
        println("SUCCESS! =======> ")
        insertDoc1(c)
        Some(c)
      }
    }
  }

  /*
    futureCollection.onComplete {
      case Failure(e) => {
        println("============== FUTURECOLLECTION FAILED! ================")
        e.printStackTrace()
      }
      case Success(coll) =>
        println(s"successfully got collection: $coll")
        insertDoc1(coll, document1)
    }
  }
  */

  /*
    resolve()
    println("DB_RESOLVED")
    val database = connection("heroku_pzmhfhvt")
 //   val collection = database.collection[BSONCollection]("Tracks")
    val collection = database[BSONCollection]("Tracks")
    println("I GOT TRACK COLLECTION")
  */


  def insertDoc1(coll: BSONCollection): Future[Unit] = {
    val writeRes: Future[WriteResult] = coll.insert(doc)

    writeRes.onComplete {
      // Dummy callbacks
      case Failure(e) => {
        println("============== INSERTION FAILED! ================")
        e.printStackTrace()
      }
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }
    writeRes.map(_ => {}) // in this example, do nothing with the success
  }

  def connect1(): Try[MongoConnection] = {
    val driver = new MongoDriver
    MongoConnection.parseURI(uri).map { parsedURI =>
      driver.connection(parsedURI)
    }

  }


}




































  /*
    /**
      * http://reactivemongo.org/releases/0.11/documentation/tutorial/connect-database.html
      */
    def resolve() = {
      val driver = new MongoDriver
      println("RESOLVING_DB...")
      val database = for {
        uri <- Future.fromTry(MongoConnection.parseURI(mongoUri))
        con = driver.connection(uri)
        dn <- Future(uri.db.get)
      } yield database

      database.onComplete {
        case resolution =>
          println(s"DB resolution: $resolution")
          driver.close()
      }
    }
    */

 //   songs.foreach(s => JsonController.writeJSON(s.id, s.attributes.asJava))
