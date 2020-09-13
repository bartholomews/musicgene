package io.bartholomews.musicgene.model.genetic

import io.bartholomews.musicgene.model.music.MusicCollection

/**
  * Object which generates playlists given a MusicCollection and settings such as length and pool size
  */
object PlaylistsFactory {

  def generateUniquePlaylists(db: MusicCollection, poolSize: Int, size: Int, f: FitnessFunction) = {
    val p = new MusicCollection(shuffleAndTake(db, size, f).songs)
    (for(n <- 1 to poolSize) yield { shuffleAndTake(p, size, f) }).toVector
  }

  // Generate 'poolSize` playlists each containing `size` songs from the db collection in random order
  def generatePlaylists(db: MusicCollection, poolSize: Int, length: Int, f: FitnessFunction): Vector[Playlist] = {
    (for(n <- 1 to poolSize) yield { shuffleAndTake(db, length, f) }).toVector
  }

  def shuffleAndTake(db: MusicCollection, size: Int, f: FitnessFunction): Playlist = {
    new Playlist(scala.util.Random.shuffle(db.songs.distinct).take(size), f)
  }

}
