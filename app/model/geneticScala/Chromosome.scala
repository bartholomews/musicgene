package model.geneticScala

import model.constraints.Constraint
import model.music.Song

/**
  * A Chromosome is a Playlist with an Array of genes = songs
  */

// TODO genes: Array/Seq/List[String/Bits/Song]
class Chromosome(val genes: Array[String]) {
  // array of songs encoded as indexes (permutation encoding)
  //val genes: Array[Int] = util.Random.shuffle(0 to size).toArray

  def get(index: Int) = genes(index)
  def set(index: Int, value: String) = { genes(index) = value }
  def size = genes.length

  override def toString: String = genes.toString

}
