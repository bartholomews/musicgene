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
    def extractSongs: Vector[Song] = JsonController.getFiles.toVector.map(f => parseSong(f))

    /*
    If an empty parameter is given, it will retrieve ALL songs from the db
    (but it should instead retrieve only the USER's songs)
     */
    /*
    def extractSongs(ids: Vector[String]): Vector[Song] = {
        if(ids.isEmpty) extractSongs else ids.flatMap(s => extractSong(s))
    }
    */

    private def parseSong(file: String): Song = {
        val list = controllers.JsonController.readJSON(file).toVector
        val id = list(0)
        val attributes = list.drop(1).map(a => {
            val tuple: Array[String] = a.split(": ")
            extractAttribute((tuple(0), tuple(1)))
        }).toSet
        new SpotifySong(id, attributes)
    }

    def get(id: String): Option[Song] = {
        // TODO
        None
    }

    // TODO reflection or metaP could work here
    private def extractAttribute(tuple: (String, String)): Attribute =
        tuple match {
            case ("Preview_URL", value) => Preview_URL(value)
            case ("Title", value) => Title(value)
            case ("Mode", value) => Mode(value.toInt)
            case ("Time_Signature", value) => Time_Signature(value.toInt)
            case ("Tempo", value) => Tempo(value.toDouble)
            case ("Energy", value) => Energy(value.toDouble)
            case ("Loudness", value) => Loudness(value.toDouble)
            case ("Key", value) => Key(value.toInt)
            case ("Artist", value) => Artist(value)
            case ("Speechiness", value) => Speechiness(value.toDouble)
            case ("Album", value) => Album(value)
            case ("Acousticness", value) => Acousticness(value.toDouble)
            case ("Duration", value) => Duration(value.toDouble)
            case ("Valence", value) => Valence(value.toDouble)
            case ("Instrumentalness", value) => Instrumentalness(value.toDouble)
            case ("Liveness", value) => Liveness(value.toDouble)
            case ("Danceability", value) => Danceability(value.toDouble)
            case (unsupported, _) => throw new Exception(unsupported + ": Attribute not matched")
        }

}
