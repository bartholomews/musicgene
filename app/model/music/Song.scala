package model.music

import java.util.concurrent.TimeUnit

import controllers.SpotifyJavaController

/**
  *
  */

trait Song {
  val id: String
  val attributes: Set[Attribute]
  def getOrElse(that: Attribute): Attribute = attributes.find(a => a.getClass == that.getClass) match {
    case None => that
    case Some(a) => a
  }
  def find(that: Attribute): String = getOrElse(that).value.toString
}

case class SpotifySong(id: String, attributes: Set[Attribute]) extends Song {

  val duration: Double = getOrElse(Duration(0)).value.asInstanceOf[Double]

  // =============================================================================
  // STRING VALS
  // =============================================================================
  /*
  Ok for view display, but it's not nice
  and it will throw an exception if used
  at the back end if the attribute is not found
  ("unknown" parsed to another data type),
  should be using Option[value] for that.
 */
  val durationToString = {
    val seconds: Int = ((duration / 1000) % 60).toInt
    val minutes = ((duration / (1000*60)) % 60).toInt
    "%02d".format(minutes) + ":" + "%02d".format(seconds)
  }

  val title = find(Title(""))
  val preview_url = find(Preview_URL(""))
  val artist = find(Artist(""))
  val album = find(Album(""))
  val tempo = find(Tempo(0))
  val loudness = find(Loudness(0))

  /**
    * The more suitable for dancing,
    * the closer to 1.0 value.
    * @see http://developer.echonest.com/acoustic-attributes.html
    */
  val danceability = find(Danceability(0))
  /**
    * Voice and acoustic instruments closer to 0
    * Synthesizers, amplifiers, distortions, etc. closer to 1
    */
  val acousticness = find(Acousticness(0))
  /**
    * The more exclusively speech-like the closer to 1.0
    * Above 0.66 tracks are probably made
    * entirely of spoken words.
    * Between 0.33 and 0.66 might contain both music and speech (e.g. rap)
    * Below 0.33 most likely music
    */
  val speechiness = find(Speechiness(0))
  /**
    *  The more confident the track is live,
    *  the closer to 1.0
    *  Above 0.8 strong likelihood for live track,
    *  below 0.6 most likely studio recordings
    */
  val liveness = find(Liveness(0))

  val valence = find(Valence(0))
  val energy = find(Energy(0))


  object Song
}
