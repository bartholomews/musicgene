package controllers

import com.mongodb.ServerAddress
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{MongoClient, MongoCredential}
import com.mongodb.casbah.commons.MongoDBObject
import model.music._
import util.JsonUtil

/**
  *
  */
object MongoController {

  val db: MongoDB = {
    val host = "ds017776.mlab.com"
    val port = 17776
    val dbName = "heroku_8hx2bfc5"
    val name = "admin"
    val server = new ServerAddress(host, port)
    val credentials = MongoCredential.createScramSha1Credential(name, dbName, name.toCharArray)
    val mongoClient = MongoClient(server, List(credentials))
    mongoClient(dbName)
  }

  val collection = db("tracks")

  private def alreadyInDB[T](key: String, value: T, collection: MongoCollection) = {
    val q = MongoDBObject(key -> value)
    collection.find(q).nonEmpty
  }

  def writeToDB(songs: Vector[Song]) = {
    val jsValues = JsonUtil.toJson(songs.filterNot(s => alreadyInDB("spotify_id", s.id, collection)))
    jsValues.foreach { track =>
      collection.insert(com.mongodb.util.JSON.parse(track.toString).asInstanceOf[DBObject])
    }
  }

  def readByID(id: String): Option[Song] = {
    val query = MongoDBObject("spotify_id" -> id)
    collection.findOne(query) match {
      case None => None
      case Some(e) => Some(parseDBObject(e))
    }
  }

  def readAll: Vector[Song] = {
    val spotify_id = "spotify_id" $exists true
    collection.find(spotify_id).map(e => parseDBObject(e)).toVector
  }

  def parseDBObject(obj: DBObject): Song = {
    Song(obj.get("spotify_id").asInstanceOf[String],
      obj.get("attributes").asInstanceOf[DBObject].map(a => MusicUtil.extractAttribute(a._1, a._2.toString)).toSet)
  }

}
