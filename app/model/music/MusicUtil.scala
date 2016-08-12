package model.music

import scala.reflect.runtime.{ universe => ru }

import com.wrapper.spotify.models.{AudioFeature, Track}
import model.constraints._
import play.api.libs.json.{JsLookupResult, JsUndefined, JsValue}
/**
  *
  */
object MusicUtil {


  /**
    * @see http://stackoverflow.com/a/1642012
    *
    * @param clazz
    * @param args
    * @return
    */
  def instantiate(clazz: java.lang.Class[_])(args:AnyRef*): AnyRef = {
    val constructor = clazz.getConstructors()(0)
    constructor.newInstance(args:_*).asInstanceOf[AnyRef]
  }

  // SETTINGS
  val penalty: Double = 100.00  // TODO it should be constraint-specific
  val tolerance: Double = 1.00  // TODO it should be constraint-specific

  def toSong(t: (Track, AudioFeature)): Song = {
    println("P_URL: " + t._1.getPreviewUrl)
    SpotifySong(t._1.getId,
      Set[Attribute](
        Preview_URL(t._1.getPreviewUrl),
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
    val name = (js \ "name").as[String]
    val constraints = js \ "constraints"
    if(constraints.isInstanceOf[JsUndefined]) { (name, Set()) }
    else {
      (name,
        (constraints \\ "constraint").map(c => {
            val args = getBoxedArgs(c)
            val cls = Class.forName("model.constraints." + (c \ "name").as[String])
            instantiate(cls)(args._1, args._2, args._3, penalty.asInstanceOf[AnyRef]).asInstanceOf[Constraint]

          /*
            val ctr = cls.getConstructors()(0)
            println("CTR: " + ctr.toString)
            val parsedArgs = Array[AnyRef](
              args._1,
              args._2,
              args._3,
              penalty.asInstanceOf[AnyRef])
            val instance = ctr.newInstance(parsedArgs: _*).asInstanceOf[Constraint]
            instance
          */

            /*
            IncludeSmaller(
              args._1,
              args._2,
              args._3,
              penalty * 2
            )
            */
            /*
          case "IncludeLarger" => IncludeLarger(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseAudioAttribute(c),
            penalty * 2
          )
          case "IncludeEquals" => IncludeEquals(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseAudioAttribute(c),
            tolerance, penalty * 2
          )

          case "ConstantRange" => ConstantRange(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseAudioAttribute(c, penalty)
          )
          case "IncreasingRange" => IncreasingRange(
            from = getIndexes(c)._1,
            to = getIndexes(c)._2,
            that = parseAudioAttribute(c, penalty)
          )
          case "DecreasingRange" => {
            val that = parseAudioAttribute(c, penalty)
            DecreasingRange(
              from = getIndexes(c)._1,
              to = getIndexes(c)._2,
              that,
              x => x < that.value
            )
          }
          */

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

          //case unknown => throw new Exception(unknown + ": constraint not found")

        }).toSet)
    }
  }

  def getBoxedArgs(js: JsValue) = {
    (
      getInt(js, "from").asInstanceOf[AnyRef],
      getInt(js, "to").asInstanceOf[AnyRef],
      parseAudioAttribute(js).asInstanceOf[AnyRef]
      )
  }

  def getInt(js: JsValue, key: String): Int = (js \ key).as[String].toInt

  def parseIndexedConstraint(constraint: (String, Int, List[String])): Constraint = constraint match {
    //    case ("Exclude", index, attr) => Exclude(index, parseAttribute(attr))
    //    case ("Include", index, attr) => Include(index, parseAttribute(attr))
    case (name, _, _) => throw new Exception(name + ": constraint not found")
  }

  def parseAudioAttribute(js: JsValue): AudioAttribute = {
    if ((js \ "attribute" \ "value").isInstanceOf[JsUndefined]) {
      println("WTF!!!!!!!!")
      parseAudioAttribute(js, penalty)
    }
    else {
      val value = (js \ "attribute" \ "value").as[String].toDouble
      parseAudioAttribute(js, value)
    }
  }

  /**
    * TODO catch errors
    *
    * @param js
    * @return
    */
  def parseAudioAttribute(js: JsValue, value: Double): AudioAttribute = {
    val cls = Class.forName("model.music." + (js \ "attribute" \ "name").as[String])
    val ctr = cls.getConstructors()(0)
    val args = Array[AnyRef](value.asInstanceOf[AnyRef])
    val instance = ctr.newInstance(args: _*).asInstanceOf[AudioAttribute]
    instance
  }
    /*
    val typee = theConstr.getParameters()(0).getType
    println("CONSTR: " + theConstr.toString)
    println("PARATYPE: " + typee.toString)
    val typeTag = getType(typee)
    println("TYPE_TAG: " + typeTag)
    val theType = Class.forName(typeTag.typeSymbol.asClass.fullName)
    */
    /*
    (js \ "attribute" \ "name").as[String] match {
      case "Acousticness" => Acousticness(value.toDouble)
      case "Danceability" => Danceability(value.toDouble)
        /*
      case "Duration" =>
        val tuple = value.split(":")
        val result = (tuple(0).toDouble * 60000) + (tuple(1).toDouble * 1000)
        println("PARSED " + value + " as " + result)
        Duration(result)
        */
      case "Liveness" => Liveness(value.toDouble)
      case "Loudness" => Loudness(value.toDouble)
      case "Speechiness" => Speechiness(value.toDouble)
      case "Tempo" => instance // Tempo(value.toDouble)
      case "Year" => Year(value.toInt)
      case unknown => throw new Exception(unknown + ": attribute not found")
    }
    */

}