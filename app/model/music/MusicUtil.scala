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

}