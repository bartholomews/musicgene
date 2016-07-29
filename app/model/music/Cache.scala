package model.music

import controllers.JsonController
import scala.collection.JavaConversions._

/**
  *
  */
object Cache {

    def extractSong(id: String): Option[Song] = JsonController.getFile(id) match {
        case null => None
        case str => Some(parseSong(str))
    }

    // do pattern matching with Option and flatmap to get only the not null ones,
    // or better query the external API for the nulls
    // well in this case you should have for sure, you're retrieving all of them in Json
    def extractSongs: Vector[Song] = JsonController.getFiles.toVector.map(f => parseSong(f))

    /*
    If an empty parameter is given, it will retrieve ALL songs from the db
    (but it should instead retrieve only the USER's songs)
     */
    def extractSongs(ids: Vector[String]): Vector[Song] = {
        if(ids.isEmpty) extractSongs else ids.flatMap(s => extractSong(s))
    }

    private def parseSong(file: String): Song = {
        val list = controllers.JsonController.readJSON(file).toVector
        val id = list(0)
        val preview = list(1)
        val attributes = list.drop(2).map(a => {
            val tuple: Array[String] = a.split(": ")
            extractAttribute((tuple(0), tuple(1)))
        }).toSet
        new Song(id, preview, attributes)
    }

    def get(id: String): Option[Song] = {
        // TODO
        None
    }

    // TODO reflection or metaP could work here
    private def extractAttribute(tuple: (String, String)): Attribute =
        tuple match {
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