package model.music

import java.time.LocalTime
import com.wrapper.spotify.models.{Track, AudioFeature}

/**
  *
  */
class MusicCollection(val songs: List[(Track, AudioFeature)]) {

  /**
    * SHOULD CALL ALL DATA ONCE AND FOR ALL, STORING IT IN A NEW DATA STRUCTURE E.G. SONGS
    *
    * @see http://stackoverflow.com/a/9214822 for conversion from seconds to time format
    * @return
    */
  override def toString = {
    var str = ""
    songs.foreach(t => str += "=================================================" + '\n' +
      t._1.getArtists.get(0).getName + ", '" + t._1.getName + "' (" + t._1.getAlbum.getName + ") "
      + "[" + LocalTime.ofSecondOfDay(t._1.getDuration / 1000).toString + "]" +
      '\n' + "ID: " + t._1.getId + " POP:" + t._1.getPopularity + '\n' +
      "Acousticiness: "  + t._2.getAcousticness + "Tempo: " + t._2.getTempo +
      "=================================================" + '\n'
    )
    str
  }

}

class Song(val id: String, val attributes: List[Attribute]) {
  override def toString: String = attributes.toString()
}

/*
case class Song(track: Track, features: AudioFeature) {
  lazy val albumName: String = track.getAlbum.getName
  lazy val artistName: String = track.getArtists.get(0).getName
  lazy val duration: Double = track.getDuration
  lazy val id: String = track.getId
  lazy val name: String = track.getName
  lazy val popularity: Int = track.getPopularity

  val attribute = features

  lazy val acousticness: Double = features.getAcousticness
  lazy val danceability: Double = features.getDanceability
  lazy val energy: Double = features.getEnergy
  lazy val instrumentalness: Double = features.getInstrumentalness
  lazy val key: Int = features.getKey
  lazy val liveness: Double = features.getLiveness
  lazy val loudness: Double = features.getLoudness
  lazy val mode: Int = features.getMode
  lazy val speechiness: Double = features.getSpeechiness
  lazy val tempo: Double = features.getTempo
  lazy val time_signature: Int = features.getTimeSignature
  lazy val songType: String = features.getType
  lazy val valence: Double = features.getValence
}
*/
