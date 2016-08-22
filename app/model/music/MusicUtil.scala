package model.music

import com.fasterxml.jackson.annotation.JsonValue

import scala.reflect.runtime.{universe => ru}
import com.wrapper.spotify.models.{AudioFeature, Track}
import model.constraints._
import play.api.libs.json._

import scala.runtime.RichInt
/**
  *
  */
object MusicUtil {

  /**
    * @see http://stackoverflow.com/a/1642012
    * @param cls
    * @param args
    * @return
    */
  private def instantiate(cls: java.lang.Class[_])(args: AnyRef*): AnyRef = {
    val constructor = cls.getConstructors()(0)
    constructor.newInstance(args: _*).asInstanceOf[AnyRef]
  }

  // SETTINGS
  val penalty = Double.MaxValue // TODO it should be constraint-specific
  // val tolerance: Double = 1.00  // TODO it should be constraint-specific

  def parseNumberOfTracks(js: JsValue): Int = (js \ "numberOfTracks").as[Int]

  def parseIDS(js: JsValue): Vector[String] = {
    val ids = js \ "ids"
    if (ids.isInstanceOf[JsUndefined]) {
      // no songs selected, should get 'n' random if constraint is given from user db
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
    * "constraints": [{"constraint": [{"name": 'c_name', "attribute": {"name" : 'a_name', "value" : 'a_value'}]}]
    *
    * @param js
    * @return
    */
  def parseRequest(js: JsValue): (String, Set[Constraint]) = {
    val name = (js \ "name").as[String]
    val constraints = js \ "constraints"
    if (constraints.isInstanceOf[JsUndefined]) {
      (name, Set())
    }
    else {
      (name,
        (constraints \\ "constraint").map(c => {

          // TODO should create key: 'constraint-type' to determine constructor args
          // i.e. type: { MonotonicConstraint | UnaryConstraint }
          val cls = Class.forName("model.constraints." + (c \ "name").as[String])
          val args = getBoxedArgs(c)
          val ok = instantiate(cls)(args._1, args._2, args._3 /*, penalty.asInstanceOf[AnyRef]*/).asInstanceOf[Constraint]
          println(ok.toString)
          ok

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
          //case unknown => throw new Exception(unknown + ": constraint not found")

        }).toSet)
    }
  }

  def getBoxedArgs(js: JsValue) = {
    (getInt(js, "from").asInstanceOf[AnyRef],
      getInt(js, "to").asInstanceOf[AnyRef],
      parseAttribute(js).asInstanceOf[Attribute])
  }

  def getInt(js: JsValue, key: String): Int = (js \ key).as[String].toInt

  def parseAttribute(js: JsValue): Attribute = {
    val value = (js \ "attribute" \ "value").asOpt[String].getOrElse(penalty.toString)
    // pattern match each attribute data type and convert value to that?
    parseAttribute(js, value.toDouble)
  }

  def parseAttribute[T](js: JsValue, value: T): Attribute = {
    val cls = Class.forName("model.music." + (js \ "attribute" \ "name").as[String])
    val ctr = cls.getConstructors()(0)
    val args = Array[AnyRef](value.asInstanceOf[AnyRef])
    ctr.newInstance(args: _*).asInstanceOf[Attribute]
  }

  /*
    val paraType = theConstr.getParameters()(0).getType
    val typeTag = getType(paraType)
    val classType = Class.forName(typeTag.typeSymbol.asClass.fullName)
    */

  /*
    (js \ "attribute" \ "name").as[String] match {
      case unknown => throw new Exception(unknown + ": attribute not found")
    }
    */

  /*
  def parseIndexedConstraint(constraint: (String, Int, List[String])): Constraint = constraint match {
    //    case ("Exclude", index, attr) => Exclude(index, parseAttribute(attr))
    //    case ("Include", index, attr) => Include(index, parseAttribute(attr))
    case (name, _, _) => throw new Exception(name + ": constraint not found")
  }
  */

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

}