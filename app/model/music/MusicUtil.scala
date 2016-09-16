package model.music

import com.wrapper.spotify.models.{AudioFeature, Track}
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

/**
  *
  */
object MusicUtil {

  def toJson(songs: Seq[Song]): Seq[JsValue] = songs.map(s => toJson(s))

  def toJson(song: Song): JsValue = JsObject(Seq(
    "spotify_id" -> JsString(song.id),
    "attributes" -> JsObject(toJsonAttribute(song.attributes))
  ))

  /**
    * @param attr
    * @return
    */
  private def toJsonAttribute(attr: Set[Attribute]): Map[String, JsValue] = {
    attr.map(a => {
      val name = a.getClass.getSimpleName
      a match {
        case _:AudioAttribute => name -> JsNumber(a.value.asInstanceOf[Double])
        case _:TimeAttribute => name -> JsNumber(a.value.asInstanceOf[Int])
        case _:TextAttribute => name -> JsString(a.value.asInstanceOf[String])
        case x => throw new Exception(x + ": Attribute value type is unknown")
      }
      // @see http://stackoverflow.com/a/2925643
    })(collection.breakOut)
  }

  // TODO reflection getName.getSimpleName pattern matched on Attribute type
  def extractAttribute(tuple: (String, String)): Attribute =
    tuple match {
        // TextAttribute
      case ("Preview_URL", value) => Preview_URL(value)
      case ("Title", value) => Title(value)
      case ("Artist", value) => Artist(value)
      case ("Album", value) => Album(value)
        // AudioAttribute
      case ("Tempo", value) => Tempo(getDoubleOrMax(value))
      case ("Energy", value) => Energy(getDoubleOrMax(value))
      case ("Loudness", value) => Loudness(getDoubleOrMax(value))
      case ("Speechiness", value) => Speechiness(getDoubleOrMax(value))
      case ("Acousticness", value) => Acousticness(getDoubleOrMax(value))
      case ("Duration", value) => Duration(getDoubleOrMax(value))
      case ("Valence", value) => Valence(getDoubleOrMax(value))
      case ("Instrumentalness", value) => Instrumentalness(getDoubleOrMax(value))
      case ("Liveness", value) => Liveness(getDoubleOrMax(value))
      case ("Danceability", value) => Danceability(getDoubleOrMax(value))
      // TimeAttribute
      case ("Key", value) => Key(value.toInt)
      case ("Mode", value) => Mode(value.toInt)
      case ("Time_Signature", value) => Time_Signature(value.toInt)
      case (unsupported, _) => throw new Exception(unsupported + ": Attribute not matched")
    }

  // http://stackoverflow.com/a/9542430
  private def getDoubleOrMax(value: String): Double = {
    try { value.toDouble } catch { case _: Throwable => Double.MaxValue }
  }

  def toSongs(songs: Vector[(Track, AudioFeature)]): Vector[Song] = songs.map(t => toSong(t))

  def toSong(t: (Track, AudioFeature)): Song = {
    Song(t._1.getId,
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

}
