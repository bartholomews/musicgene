package model.music

import java.util.UUID._

import io.bartholomews.spotify4s.entities.{AudioFeatures, FullTrack}
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
        case _: AudioAttribute => name -> JsNumber(a.value.asInstanceOf[Double])
        case _: TimeAttribute => name -> JsNumber(a.value.asInstanceOf[Int])
        case _: TextAttribute => name -> JsString(a.value.asInstanceOf[String])
        case x => throw new Exception(x + ": Attribute value type is unknown")
      }
      // @see http://stackoverflow.com/a/2925643
    }).toMap
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
    try {
      value.toDouble
    } catch {
      case _: Throwable => Double.MaxValue
    }
  }

  def toSongs(songs: Seq[(FullTrack, AudioFeatures)]): Seq[Song] = songs.map(t => toSong(t._1, t._2))

  def personalSong(t: FullTrack): Song = Song(randomUUID().toString, Set())

  def toSong(t: FullTrack, af: AudioFeatures): Song = {
    Song(t.id.map(_.value).getOrElse("N/A"), // .getOrElse(randomUUID().toString
      Set[Attribute](
        Preview_URL(t.previewUrl.map(_.renderString).getOrElse("")),
        Title(t.name),
        Album(t.album.name),
        Artist(t.artists.head.name),
        Duration(t.durationMs),
        Acousticness(af.acousticness.value),
        Danceability(af.danceability.value),
        Energy(af.energy.value),
        Instrumentalness(af.instrumentalness.value),
        Liveness(af.liveness.value),
        Loudness(af.loudness),
        Speechiness(af.speechiness.value),
        Tempo(af.tempo),
        Valence(af.valence.value),
        Time_Signature(af.timeSignature),
        Mode(af.mode.value),
        Key(af.key.value)
      )
    )
  }

  def toSong(t: FullTrack): Song = {
    Song(t.id.map(_.value).getOrElse("N/A"), // .getOrElse(randomUUID().toString),
      Set[Attribute](
        Preview_URL(t.previewUrl.map(_.renderString).getOrElse("")),
        Title(t.name),
        Album(t.album.name),
        Artist(t.artists.head.name),
        Duration(t.durationMs)
      ))
  }

  def millisecondsToMinutesAndSeconds(timeUnitMs: Int): String = {
    val minutes = (timeUnitMs / 1000)  / 60
    val seconds = (timeUnitMs / 1000) % 60
    s"$minutes:$seconds"
  }
}
