package controllers

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import model.music._

/**
  * Object to interface with a MongoDB server using the Casbah driver
  */
object MongoController {

  /**
    * Connect to the MongoDB server at the specified URI, retrieving the
    * requested collection `collectionName` into the database `dbName`
    *
    * @param uri the full URI to connect to a MongoDB database server
    *            should have the form:
    *            mongodb://[username:password@]host1[:port1]
    *            [,host2[:port2], ... [/[database][?options] ]
    *            { @see https://docs.mongodb.com/manual/reference/connection-string/ }
    * @param dbName the name of the database to connect to
    * @param collectionName the name of the collection in the database
    * @return the requested MongoDB collection
    */
  def getCollection(uri: String, dbName: String, collectionName: String): MongoCollection = {
    MongoClient(MongoClientURI(uri))(dbName)(collectionName)
  }

  /**
    * Check if a particular value referenced by a specific key
    * is already stored in a collection
    *
    * @param key the key which will determine the records to test
    * @param value the value to test
    * @param collection the MongoDB collection on which to run the query
    * @return true if the value passed as argument is already store in the collection
    *         under that particular key, false otherwise
    */
  def alreadyInDB[T](key: String, value: T, collection: MongoCollection) = {
    val q = MongoDBObject(key -> value)
    collection.find(q).nonEmpty
  }

  /**
    * Convert songs to JSON and write them to the MongoDB collection
    * if not already stored
    *
    * @param collection the collection where songs are inserted
    * @param songs the Song instances to be written to the database
    */
  def writeToDB(collection: MongoCollection, songs: Vector[Song]) = {
    val jsValues = MusicUtil.toJson(songs.filterNot(s => alreadyInDB("spotify_id", s.id, collection)))
    jsValues.foreach { track =>
      collection.insert(com.mongodb.util.JSON.parse(track.toString).asInstanceOf[DBObject])
    }
  }

  def writeToDB(collection: MongoCollection, song: Song) = {
    collection.insert(com.mongodb.util.JSON.parse(MusicUtil.toJson(song).toString).asInstanceOf[DBObject])
  }

  /**
    * Look for a specific "spotify_id": if found, is converted
    * and returned as Some(Song), otherwise it will return None
    *
    * @param collection the collection where the "spotify_id" is to be looked up
    * @param value the "spotify_id" to be looked up
    * @return an instance of Song with that "spotify_id" wrapped in an Option, or None if not found
    */
  def readByID(collection: MongoCollection, value: String): Option[Song] = {
    val query = MongoDBObject("spotify_id" -> value)
    collection.findOne(query) match {
      case None => None
      case Some(e) => Some(parseSong(e))
    }
  }

  /**
    * Retrieve all the values with key "spotify_id" in a particular MongoDB collection
    *
    * @param collection the collection where the "spotify_id" values are to be retrieved
    * @return a Vector of String with the spotify ids retrieved
    */
  def readIDs(collection: MongoCollection): Vector[String] = {
    val spotify_id = "spotify_id" $exists true
    collection.find(spotify_id).flatMap(e => getID(e)).toVector
  }

  /**
    * Convert a MongoDB DBObject into an instance of Song.
    *
    * @param obj the DBObject representing a Song. It should have been parsed as:
    *            "spotify_id" -> a JsString with the Spotify ID
    *            "attributes" -> a JsObject wrapping String -> JsValue
    *            (which for instance could be a JsString or a JsNumber
    *            depending if it is a TextAttribute or AudioAttribute)
    * @return an instance of the extracted Song
    */
  def parseSong(obj: DBObject): Song = {
    Song(getID(obj),
      obj.get("attributes").asInstanceOf[DBObject]
        .map(a => MusicUtil.extractAttribute(a._1, a._2.toString)).toSet)
  }

  /**
    *
    * Get the String value of a DBObject for key "spotify_id"
    *
    * @param obj the DBObject representing a Song instance
    * @return the Spotify ID of that object
    */
  def getID(obj: DBObject): Option[String] = {
    obj.get("spotify_id").asInstanceOf[Option[String]]
  }

  /**
    * Read and extract as Song the first `n` records in the collection
    *
    * @param collection the collection from where to retrieve the Song instances
    * @param n the number of records to retrieve
    * @return a Vector wrapping the retrieved Song instances
    */
  def read(collection: MongoCollection, n: Int): Vector[Song] = {
    val spotify_id = "spotify_id" $exists true
    collection.find(spotify_id).limit(n).map(e => parseSong(e)).toVector
  }

  /**
    * Read and extract as Song all the records in the collection
    *
    * @param collection the collection from where to retrieved the Song instances
    * @return a Vector wrapping the retrieved Song instances
    */
  def readAll(collection: MongoCollection): Vector[Song] = {
    val spotify_id = "spotify_id" $exists true
    collection.find(spotify_id).map(e => parseSong(e)).toVector
  }

}
