package model.geneticScala

import model.music.{Song, Title}

import scala.util.Random

/**
  *
  */
class Playlist(val songs: Vector[Song]) {

  def get(index: Int) = songs(index)
  def size = songs.length

  // ok now really i want to get rid of f here, should really go into pop
  //def fitness: Float = f.getFitness(this)

  def prettyPrint() = {
    songs.foreach(s => {
      println("- " + s.title + "(T: " + s.tempo + ", L: " + s.loudness + ")")
    })
  }

  // order changing: songs in a playlist are swapped
  // maybe it's cost-effective to just swap 2 songs
  // and move the mutationRatio check in the Population method caller
  def mutate: Playlist = {
    val arr = songs.toArray
    for(i <- songs.indices) {
      if (Random.nextFloat() < GASettings.mutationRatio) {
        val j = Random.nextInt(songs.length)
        val aux = arr(i)
        arr(i) = arr(j)
        arr(j) = aux
      }
    }
    new Playlist(arr.toVector)
  }

  // single point crossover:
  //  one crossover point is selected, the permutation is copied
  // from the first parent till the crossover point,
  // then the other parent is scanned and if the number
  // is not yet in the offspring, it is added
  // Note: there are more ways how to produce the rest after crossover point,
  // maybe better to move the pivot to have the fittest playlist ???
  def crossover(that: Playlist) = {
    val pivot = Random.nextInt(songs.length)
    val v1 = this.songs.take(pivot)
    // some songs are dropped randomly. Need to improve that.
    val v2 = that.songs.filter(s => !v1.contains(s)).take(that.songs.length - pivot)
    new Playlist(v1 ++ v2)
  }

}
