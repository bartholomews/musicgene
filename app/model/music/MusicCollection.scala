package model.music

import com.wrapper.spotify.models.{Track, AudioFeature}

/**
  *
  */
class MusicCollection(val songs: Vector[Song]) {

    /*
  def this(IDs: Vector[String]) {
    this(Cache.extractSongs(IDs))
  }
  */

  /*
  def this(list: List[(Track, AudioFeature)]) {
    this(MusicUtil.toSongs(list))
  }
  */

  def apply(i: Int) = songs(i)
  def length = songs.length
  def IDs: Vector[String] = songs.map(t => t.id)
  /**
    * @param p the predicate which filter out songs
    * @return a new `MusicCollection` with the songs filtered by a predicate
    */
  def filter(p: Boolean) = new MusicCollection(this.songs.filter(s => p))
  /**
    * SHOULD CALL ALL DATA ONCE AND FOR ALL, STORING IT IN A NEW DATA STRUCTURE E.G. SONGS
    *
    * @see http://stackoverflow.com/a/9214822 for conversion from seconds to time format
    * @return
    */
  override def toString = songs.toString

  // wtf is that
  def prettyPrintTitleArtist() = {
    songs.foreach(s => {
      s.attributes.foreach {
        case Artist(name) => print("[ARTIST: " + name + "] ")
        case Title(title) => print("[TITLE: " + title + "] ")
        case _ =>
      }
      println
    })
  }

}