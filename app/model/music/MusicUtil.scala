package model.music

import com.fasterxml.jackson.annotation.JsonValue
import com.wrapper.spotify.models.{AudioFeature, Track}
import model.constraints._
import play.api.libs.json.{JsLookupResult, JsUndefined, JsValue}

/**
  *
  */
object MusicUtil {

  def toSongs(songs: Vector[(Track, AudioFeature)]): Vector[Song] = {
    songs.map(t => new Song(t._1.getId,
      Set[Attribute](
        Title(t._1.getName),
        Album(t._1.getAlbum.getName),
        Artist(t._1.getArtists.get(0).getName),
        Duration(t._1.getDuration),
        Acousticness(t._2.getAcousticness),
        Danceability(t._2.getDanceability),
        Energy(t._2.getEnergy),
        Instrumentalness(t._2.getInstrumentalness),
        Liveness(t._2.getLiveness),
        Loudness(t._2.getLoudness),
        Speechiness(t._2.getSpeechiness),
        Tempo(t._2.getTempo),
        Valence(t._2.getValence),
        Time_Signature(t._2.getTimeSignature),
        Mode(t._2.getMode),
        Key(t._2.getKey)
      )
    ))
  }

  def parseIDS(js: JsValue): Vector[String] = {
    // checking ids(TODO move to its own method)
    val ids = js \ "ids"
    // TODO you might get rid of this
    if (ids.isInstanceOf[JsUndefined]) {
      // no songs selected, should get 20 random/'n' if constraint is given from user db
      println("======> NO SONGS SELECTED")
      Vector()
    } else {
      (ids \\ "id").map(js => js.as[String]).toVector
    }
  }

  // TODO could change json format with key type for indexed, standard etc
  // again reflection todo
  /**
    * parse json of the form
    * { "name": 'playlistName', "ids": [{"id": 'id1'}, {"id": 'id2'}],
    *   "constraints": [{"constraint": [{"name": 'c_name', "attribute": 'c_attribute'}]}]
    * TODO do later
    *
    * @param js
    * @return
    */
  def parseRequest(js: JsValue): (String, Set[Constraint]) = {
    val name = (js \ "name").as[String]
    // checking constraints(TODO move to its own method)
    if((js \ "constraints").isInstanceOf[JsUndefined]) {
      //      println("======> CONSTRAINTS UNDEFINED")
      (name, Set())
    }
    else {
      (name,
        (js \ "constraints" \\ "constraint").map(c => (c \ "name").as[String] match {
          case "Include" => Include((c \ "index").as[Int], parseJsonAttribute((c \ "attribute").get))
          case "Exclude" => Exclude((c \ "index").as[Int], parseJsonAttribute((c \ "attribute").get))
          case "IncludeSmaller" => IncludeSmaller((c \ "index").as[Int],
            parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])
          case "IncludeLarger" => IncludeLarger((c \ "index").as[Int],
            parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])

          //     case "ExcludeAll" => ExcludeAll(parseJsonAttribute((c \ "attribute").get))

          case "UnaryLargerAll" => UnaryLargerAll(parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])
          case "UnaryLargerAny" => UnaryLargerAny(parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])
          case "UnaryLargerNone" => UnaryLargerNone(parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])
          case "UnarySmallerAll" => UnarySmallerAll(parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])
          case "UnarySmallerAny" => UnarySmallerAny(parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])
          case "UnarySmallerNone" => UnarySmallerNone(parseJsonAttribute((c \ "attribute").get).asInstanceOf[AudioAttribute])

          case unknown => throw new Exception(unknown + ": constraint not found")
        }).toSet)
    }
  }

  def parseIndexedConstraint(constraint: (String, Int, List[String])): Constraint = constraint match {
    //    case ("Exclude", index, attr) => Exclude(index, parseAttribute(attr))
    //    case ("Include", index, attr) => Include(index, parseAttribute(attr))
    case (name, _, _) => throw new Exception(name + ": constraint not found")
  }

  // TODO: REFLECTION, or at least intercept TimeAttribute to assign Double etc.
  def parseJsonAttribute(js: JsValue): Attribute = {
    val value = (js \ "value").as[String]
    (js \ "name").as[String] match {
      case "Acousticness" => Acousticness(value.toDouble)
      case "Duration" =>
        val tuple = value.split(":")
        val result = (tuple(0).toDouble * 60000) + (tuple(1).toDouble * 1000)
        println("PARSED " + value + " as " + result)
        Duration(result)
      case "Loudness" => Loudness(value.toDouble)
      case "Tempo" => Tempo(value.toDouble)
      case "Year" => Year(value.toInt)
      case unknown => throw new Exception(unknown + ": attribute not found")
    }
  }

  // TODO: REFLECTION
  // this approach has less JavaScript parsing and input overhead
  def parseAttribute(json: List[String]): Attribute = json match {
    case name :: value => name match {
      case "Acousticness" => value match {
        case v :: Nil => Acousticness(v.toDouble)
        case _ => throw new Exception("Acousticness has a bad format")
      }
      case "Artist" => value match {
        case v :: Nil => Artist(v)
        case _ => throw new Exception("Artist has a bad format")
      }
      case "Year" => value match {
        case v :: Nil => Year(v.toInt)
        case _ => throw new Exception("Year has a bad format")
      }
      case "Duration" => value match {
        case v :: Nil => Duration(v.toInt)
        case _ => throw new Exception("Duration has a bad format")
      }
    }
    case _ => throw new Exception("Attribute has a bad format")
  }


}