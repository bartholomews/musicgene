package model.music

import java.util.concurrent.TimeUnit

import controllers.SpotifyController

/**
  *
  */
class Song(val id: String, val attributes: Set[Attribute]) {

  def getAttribute(that: Attribute): Option[Attribute] = attributes.find(a => a.getClass == that.getClass)

  def find(that: Attribute): String = getAttribute(that) match {
    case None => "[unknown]"
    case Some(a) => a.value.toString
  }

  val duration: Double = getAttribute(Duration(0)) match {
    case None => 0
    case Some(d) => d.value.asInstanceOf[Double]
  }

  val durationToString = {
    val seconds: Int = ((duration / 1000) % 60).toInt
    val minutes = ((duration / (1000*60)) % 60).toInt
    "%02d".format(minutes) + ":" + "%02d".format(seconds)
  }

  /*
    Ok for view display, but it's not nice
    and it will throw an exception if used
    at the back end if the attribute is not found
    ("unknown" parsed to another data type),
    should be using Option[value] for that.
   */
  val title = find(Title(""))
  val artist = find(Artist(""))
  val album = find(Album(""))
  val tempo = find(Tempo(0))
  val loudness = find(Loudness(0))
  val acousticness = find(Acousticness(0))

  object Song
}
