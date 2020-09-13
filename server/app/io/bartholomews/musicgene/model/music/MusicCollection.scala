package io.bartholomews.musicgene.model.music

/**
  *
  */
class MusicCollection(val songs: List[AudioTrack]) {

  def apply(i: Int) = songs(i)
  def size = songs.size
  def IDs: List[String] = songs.map(t => t.id)
  /**
    * @param p the predicate which filter out songs
    * @return a new `MusicCollection` with the songs filtered by a predicate
    */
  def filter(p: Boolean) = new MusicCollection(this.songs.filter(s => p))

  override def toString = songs.toString

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