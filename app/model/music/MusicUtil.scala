package model.music

import com.fasterxml.jackson.annotation.JsonValue
import com.wrapper.spotify.models.{AudioFeature, Track}
import model.constraints._
import play.api.libs.json.{JsLookupResult, JsUndefined, JsValue}

/**
  *
  */
object MusicUtil {

  // SETTINGS
  val penalty: Double = 10000.00  // TODO it should be constraint-specific
  val tolerance: Double = 10.00  // TODO it should be constraint-specific

  def toSong(t: (Track, AudioFeature)): Song = {
    new Song(t._1.getId, t._1.getPreviewUrl,
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
    )
  }

  def toSongs(songs: Vector[(Track, AudioFeature)]): Vector[Song] = songs.map(t => toSong(t))

  def parseNumberOfTracks(js: JsValue): Int = (js \ "numberOfTracks").as[Int]

  def parseIDS(js: JsValue): Vector[String] = {
    // checking ids(TODO move to its own method)
    val ids = js \ "ids"
    // TODO you might get rid of this
    if (ids.isInstanceOf[JsUndefined]) {
      // no songs selected, should get 20 random/'n' if constraint is given from user db
      println("======> NO SONGS SELECTED")
      Vector()
    } else {
      ids.as[Array[String]].toVector
    }
  }

  // TODO could change json format with key type for indexed, standard etc
  // again reflection todo
  /**
    * parse json of the form
    * { "name": 'playlistName', "ids": ['id1', 'id2', 'id3'],
    *   "constraints": [{"constraint": [{"name": 'c_name', "attribute": {"name" : 'a_name', "value" : 'a_value'}]}]
    * TODO do later
    *
    * @param js
    * @return
    */
  def parseRequest(js: JsValue): (String, Set[Constraint]) = {
    println("READY TO PARSE")
    val name = (js \ "name").as[String]
    // checking constraints(TODO move to its own method)
    println("PARSED " + name)
    if((js \ "constraints").isInstanceOf[JsUndefined]) {
      println("======> CONSTRAINTS UNDEFINED")
      (name, Set())
    }
    else {
      (name,
        (js \ "constraints" \\ "constraint").map(c => (c \ "name").as[String] match {

          case "IncludeSmaller" => IncludeSmaller(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseJsonAttribute(c).asInstanceOf[AudioAttribute],
            penalty * 2
          )
          case "IncludeLarger" => IncludeLarger(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseJsonAttribute(c).asInstanceOf[AudioAttribute],
            penalty * 2
          )
          case "IncludeEquals" => IncludeEquals(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseJsonAttribute(c).asInstanceOf[AudioAttribute],
            tolerance, penalty * 2
          )

          case "ConstantRange" => ConstantRange(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseJsonAttributeName(c, penalty.toString).asInstanceOf[AudioAttribute]
          )
          case "IncreasingRange" => IncreasingRange(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseJsonAttributeName(c, penalty.toString).asInstanceOf[AudioAttribute]
          )
          case "DecreasingRange" => DecreasingRange(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseJsonAttributeName(c, penalty.toString).asInstanceOf[AudioAttribute]
          )

          /*
          case "IncludeEquals" => {
            val that = parseJsonAttribute(c).asInstanceOf[AudioAttribute]
            val i = getIndexes(c)._1; val j = getIndexes(c)._2
            for(index <- i to j) yield { CompareWithTolerance(index, that, tolerance) }
          }

          case "IncludeLarger" => {
            val that = parseJsonAttribute(c).asInstanceOf[AudioAttribute]
            val i = getIndexes(c)._1; val j = getIndexes(c)._2
            for(index <- i to j) yield { Compare(index, that, (x, y) => x > y) }
          }

          case "IncludeSmaller" => {
            val that = parseJsonAttribute(c).asInstanceOf[AudioAttribute]
            val i = getIndexes(c)._1; val j = getIndexes(c)._2
            for(index <- i to j) yield { Compare(index, that, (x, y) => x < y) }
          }

          case "IncreasingRange" => {
            val that = parseJsonAttributeName(c, "0.0").asInstanceOf[AudioAttribute]
            val i = getIndexes(c)._1; val j = getIndexes(c)._2
            (i to j).sliding(2).map(v => CompareRange(v.head, v.tail.head, that, (x: Double, y: Double) => x < y)
            )
          }
          case "DecreasingRange" => {
            val that = parseJsonAttributeName(c, "0.0").asInstanceOf[AudioAttribute]
            val i = getIndexes(c)._1; val j = getIndexes(c)._2
            (i to j).sliding(2).map(v => CompareRange(v.head, v.tail.head, that, (x: Double, y: Double) => x > y)
            )
          }
          case "ConstantRange" => {
            val i = getIndexes(c)._1; val j = getIndexes(c)._2
            (i to j).sliding(2).map(v => {
              val that = parseJsonAttributeName(c, v.tail.head.toString).asInstanceOf[AudioAttribute]
              CompareWithTolerance(v.head, that, tolerance)
            }
            )
          }
          */

          case unknown => throw new Exception(unknown + ": constraint not found")

        }).toSet)
    }
  }

  def getIndexes(js: JsValue): (Int, Int) = {
    ((js \ "from").as[String].toInt, (js \ "to").as[String].toInt)
  }

  def parseIndexedConstraint(constraint: (String, Int, List[String])): Constraint = constraint match {
    //    case ("Exclude", index, attr) => Exclude(index, parseAttribute(attr))
    //    case ("Include", index, attr) => Include(index, parseAttribute(attr))
    case (name, _, _) => throw new Exception(name + ": constraint not found")
  }

  // TODO: REFLECTION, or at least intercept TimeAttribute to assign Double etc.
  def parseJsonAttribute(js: JsValue): Attribute = {
    val value = (js \ "attribute" \ "value").as[String]
    parseJsonAttributeName(js, value)
  }

  def parseJsonAttributeName(js: JsValue, value: String): Attribute = {
    (js \ "attribute" \ "name").as[String] match {
      case "Acousticness" => Acousticness(value.toDouble)
      case "Danceability" => Danceability(value.toDouble)
      case "Duration" =>
        val tuple = value.split(":")
        val result = (tuple(0).toDouble * 60000) + (tuple(1).toDouble * 1000)
        println("PARSED " + value + " as " + result)
        Duration(result)
      case "Liveness" => Liveness(value.toDouble)
      case "Loudness" => Loudness(value.toDouble)
      case "Speechiness" => Speechiness(value.toDouble)
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