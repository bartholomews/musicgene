package model.music

/**
  *
  */

case class Song(id: Option[String], attributes: Set[Attribute]) {

  def getOrElse(that: Attribute): Attribute = attributes.find(a => a.getClass == that.getClass) match {
    case None => that
    case Some(a) => a
  }

  def getOrElse(that: Attribute, placeholder: String): String = attributes.find(a => a.getClass == that.getClass) match {
    case None => placeholder
    case Some(a) => a.value.toString
  }

  def findValue(that: Attribute): Option[String] = attributes.find(a => a.getClass == that.getClass) match {
    case None => None
    case Some(a) => Some(a.value.toString)
  }

  def find(that: Attribute) = findValue(that).getOrElse(that.value.toString)

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
  val stringDuration = {
    val seconds: Int = ((duration / 1000) % 60).toInt
    val minutes = ((duration / (1000*60)) % 60).toInt
    "%02d".format(minutes) + ":" + "%02d".format(seconds)
  }

  val title = findValue(Title(""))
  val preview_url = findValue(Preview_URL())
  val artist = findValue(Artist(""))
  val album = findValue(Album(""))
  val tempo = findValue(Tempo(0))
  val loudness = findValue(Loudness(0))

  /**
    * The more suitable for dancing,
    * the closer to 1.0 value.
    * @see http://developer.echonest.com/acoustic-attributes.html
    */
  val danceability = findValue(Danceability(0))
  /**
    * Voice and acoustic instruments closer to 0
    * Synthesizers, amplifiers, distortions, etc. closer to 1
    */
  val acousticness = findValue(Acousticness(0))
  /**
    * The more exclusively speech-like the closer to 1.0
    * Above 0.66 tracks are probably made
    * entirely of spoken words.
    * Between 0.33 and 0.66 might contain both music and speech (e.g. rap)
    * Below 0.33 most likely music
    */
  val speechiness = findValue(Speechiness(0))
  /**
    *  The more confident the track is live,
    *  the closer to 1.0
    *  Above 0.8 strong likelihood for live track,
    *  below 0.6 most likely studio recordings
    */
  val liveness = findValue(Liveness(0))
  val valence = findValue(Valence(0))
  val energy = findValue(Energy(0))

}
