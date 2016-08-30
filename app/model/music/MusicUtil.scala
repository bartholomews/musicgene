package model.music

import com.wrapper.spotify.models.{AudioFeature, Track}
import play.api.libs.json.JsValue

/**
  *
  */
object MusicUtil {

  // TODO reflection getName.getSimpleName pattern matched on Attribute type
  def extractAttribute(tuple: (String, AnyRef)): Attribute =
    tuple match {
        // TextAttribute
      case ("Preview_URL", value) => Preview_URL(value.asInstanceOf[String])
      case ("Title", value) => Title(value.asInstanceOf[String])
      case ("Artist", value) => Artist(value.asInstanceOf[String])
      case ("Album", value) => Album(value.asInstanceOf[String])
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
      case ("Key", value) => Key(value.asInstanceOf[Int])
      case ("Mode", value) => Mode(value.asInstanceOf[Int])
      case ("Time_Signature", value) => Time_Signature(value.asInstanceOf[Int])
      case (unsupported, _) => throw new Exception(unsupported + ": Attribute not matched")
    }

  // http://stackoverflow.com/a/9542430
  private def getDoubleOrMax(value: AnyRef): Double = {
    try { value.asInstanceOf[Double] } catch { case _: Throwable => Double.MaxValue }
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
