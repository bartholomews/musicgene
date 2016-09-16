package controllers

import com.mongodb.ServerAddress
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import model.music._
import util.JsonUtil

/**
  *
  */
object MongoController {

  def getCollection(uri: String, dbName: String, collectionName: String): MongoCollection = {
    MongoClient(MongoClientURI(uri))(dbName)(collectionName)
  }

  def alreadyInDB[T](key: String, value: T, collection: MongoCollection) = {
    val q = MongoDBObject(key -> value)
    collection.find(q).nonEmpty
  }

  def writeToDB(collection: MongoCollection, songs: Vector[Song]) = {
    val jsValues = JsonUtil.toJson(songs.filterNot(s => alreadyInDB("spotify_id", s.id, collection)))
    jsValues.foreach { track =>
      collection.insert(com.mongodb.util.JSON.parse(track.toString).asInstanceOf[DBObject])
    }
  }

  def readByID(collection: MongoCollection, id: String): Option[Song] = {
    val query = MongoDBObject("spotify_id" -> id)
    collection.findOne(query) match {
      case None => None
      case Some(e) => Some(parseDBObject(e))
    }
  }

  def readIDs(collection: MongoCollection): Vector[String] = {
    val spotify_id = "spotify_id" $exists true
    collection.find(spotify_id).map(e => getID(e)).toVector
  }

  def read(collection: MongoCollection, n: Int): Vector[Song] = {
    val spotify_id = "spotify_id" $exists true
    collection.find(spotify_id).limit(n).map(e => parseDBObject(e)).toVector
  }

  def readAll(collection: MongoCollection): Vector[Song] = {
    val spotify_id = "spotify_id" $exists true
    collection.find(spotify_id).map(e => parseDBObject(e)).toVector
  }

  def getID(obj: DBObject): String = {
    obj.get("spotify_id").asInstanceOf[String]
  }

  def parseDBObject(obj: DBObject): Song = {
    Song(getID(obj),
      obj.get("attributes").asInstanceOf[DBObject]
        .map(a => MusicUtil.extractAttribute(a._1, a._2.toString)).toSet)
  }

}
