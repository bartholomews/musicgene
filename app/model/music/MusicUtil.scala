package model.music

import it.turingtest.spotify.scala.client.entities.{AudioFeatures, Track}
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

/**
  *
  */
object MusicUtil {

  def toJson(songs: Seq[Song]): Seq[JsValue] = songs.map(s => toJson(s))

  def toJson(song: Song): JsValue = JsObject(Seq(
    "spotify_id" -> JsString(song.id.orNull),
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

  def toSongs(songs: Seq[(Track, AudioFeatures)]): Seq[Song] = songs.map(t => toSong(t))

  def personalSong(t: Track): Song = Song(None, Set())

  def toSong(t: (Track, AudioFeatures)): Song = {
    Song(t._1.id,
      Set[Attribute](
        Preview_URL(t._1.preview_url.getOrElse("")),
        Title(t._1.name),
        Album(t._1.album.name),
        Artist(t._1.artists.head.name),
        Duration(t._1.duration_ms),
        Acousticness(t._2.acousticness),
        Danceability(t._2.danceability),
        Energy(t._2.energy),
        Instrumentalness(t._2.instrumentalness),
        Liveness(t._2.liveness),
        Loudness(t._2.loudness),
        Speechiness(t._2.speechiness),
        Tempo(t._2.tempo),
        Valence(t._2.valence),
        Time_Signature(t._2.time_signature),
        Mode(t._2.mode),
        Key(t._2.key)
      )
    )
  }

  def toSong(t: Track): Song = {
    Song(t.id,
      Set[Attribute](
        Preview_URL(t.preview_url.getOrElse("")),
        Title(t.name),
        Album(t.album.name),
        Artist(t.artists.head.name),
        Duration(t.duration_ms)
      ))
  }

}
