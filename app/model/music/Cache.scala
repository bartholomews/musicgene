package model.music

import controllers.JsonController
import scala.collection.JavaConversions._

/**
  *
  */
object Cache {

    def getFromCache(ids: Vector[String]): (Vector[Song], Vector[String]) = {
        val (inCache, outCache) = ids.partition(id => JsonController.isInCache(id))
        (inCache.map(s => parseSong(s)), outCache)
    }

    /*
    // do pattern matching with Option and flatmap to get only the not null ones,
    // or better query the external API for the nulls
    // well in this case you should have for sure, you're retrieving all of them in Json
    */
    def extractSongs: Vector[Song] = JsonController.getIDs.toVector.map(f => parseSong(f))

    /*
    If an empty parameter is given, it will retrieve ALL songs from the db
    (but it should instead retrieve only the USER's songs)
     */
    /*
    def extractSongs(ids: Vector[String]): Vector[Song] = {
        if(ids.isEmpty) extractSongs else ids.flatMap(s => extractSong(s))
    }
    */

    private def parseSong(stringID: String): Song = {
        val list = controllers.JsonController.readJSON(stringID).toVector
        val id = list(0)
        val attributes = list.drop(1).map(a => {
            val tuple: Array[String] = a.split(": ")
            MusicUtil.extractAttribute((tuple(0), tuple(1)))
        }).toSet
        Song(id, attributes)
    }

    def get(id: String): Option[Song] = {
        // TODO
        None
    }
}
