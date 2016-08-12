package model.geneticScala

import model.music.MusicCollection

/**
  * ALL THESE CONVERSIONS....
  * ARRAY vs LIST
  * Random access
  * That sorting looks expensive
  */
object PlaylistsFactory {

  def generateUniquePlaylists(db: MusicCollection, poolSize: Int, size: Int, f: FitnessFunction) = {
    val p = new MusicCollection(generatePlaylist(db, size, f).songs)
    (for(n <- 1 to poolSize) yield { generatePlaylist(p, size, f) }).toVector
  }

  // Generate 'poolSize` playlists each containing `size` songs from the db collection in random order
  def generatePlaylists(db: MusicCollection, poolSize: Int, size: Int, f: FitnessFunction): Vector[Playlist] = {
    (for(n <- 1 to poolSize) yield { generatePlaylist(db, size, f) }).toVector
  }

  // Generate `poolSize` playlists each containing the whole database collection in random order
  def generatePlaylists(db: MusicCollection, poolSize: Int, f: FitnessFunction): Vector[Playlist] = {
    (for (n <- 1 to poolSize) yield { generatePlaylist(db, f) }).toVector
  }

  def generatePlaylist(db: MusicCollection, size: Int, f: FitnessFunction): Playlist = {
    new Playlist(util.Random.shuffle(db.songs.distinct).take(size), f)
  }

  // playlist with the whole collection, removed duplicates
  def generatePlaylist(db: MusicCollection, f: FitnessFunction): Playlist = {
    new Playlist(util.Random.shuffle(db.songs).distinct, f)
  }

  /*  ====================================================================================================

  /**
    * Create a random `Chromosome`.
    * The `Chromosome` is a candidate playlist encoded as a class with
    * an Array of genes `Song` IDs, from the pool of `MusicCollection`.
    *
    * @param size the number of the Chromosome's genes (songs in the candidate playlist)
    * @return a newly generated Chromosome (candidate playlist)
    */
  def generateChromosome(db: MusicCollection, size: Int): Chromosome = {
    val genes: Array[String] = util.Random.shuffle(db.IDs).take(size).toArray
    new Chromosome(genes)
  }

  def generateChromosomes(db: MusicCollection, poolSize: Int, size: Int): Array[Chromosome] = {
    (for(n <- 1 to poolSize) yield { generateChromosome(db, size) }).toArray
  }

  // preprocessing version, for faster performance to quickly filter out totally irrelevant songs.
  def generateChromosome(db: MusicCollection, size: Int, p: Boolean): Chromosome = {
    val filteredCollection: MusicCollection = db.filter(p)
    val genes: Array[String] = util.Random.shuffle(filteredCollection.IDs).take(size).toArray
    new Chromosome(genes)
  }

  */

}
