package model.geneticScala

import model.music.{Song, Title}

import scala.util.Random

/**
  *
  */
class Playlist(val songs: Vector[Song], f: FitnessFunction) {

  def get(index: Int) = songs(index)

  val fitness = f.getFitness(this)
  val distance = f.getDistance(this)

  def size = songs.length

  val matched: Set[Int] = {
    f.score(this).partition(s => s.matched)._1.map(s => s.index)
  }
  val unmatched: Set[Int] = {
    f.score(this).partition(s => s.matched)._2.map(s => s.index)
  }

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
    val weakBucket = Random.shuffle(unmatched)
    for (weakIndex <- weakBucket) {
      if (Random.nextFloat() < GASettings.mutationRatio) {
        val randomIndex = Random.nextInt(songs.length)
        val aux = arr(weakIndex)
        arr(weakIndex) = arr(randomIndex)
        arr(randomIndex) = aux
      }
    }
    val newP = new Playlist(arr.toVector, f)
  //  println("MT => new playlist with size " + newP.size)
    newP // new Playlist(arr.toVector, f)

    /*
    val arr = songs.toArray
    for(i <- songs.indices) {
      if (Random.nextFloat() < GASettings.mutationRatio) {
        val j = Random.nextInt(songs.length)
        val aux = arr(i)
        arr(i) = arr(j)
        arr(j) = aux
      }
    }
    new Playlist(arr.toVector, f)
    */
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
    val v2 = that.songs.filter(s => !v1.contains(s)).take(that.songs.length - pivot)
    new Playlist(v1 ++ v2, f)
  }

  /*
  Take the indexes matched of inferior playlist
  add the indexes matched of superior playlist if not already there
  add the indexes unmatched of inferior playlist if not already there
  add the indexes unmatched of superior playlist if not already there
 */
  /*
  def crossover(that: Playlist) = {
    val v1 = that.matched.map(i => that.songs(i)).toVector
    val v2 = this.matched.map(i => this.songs(i)).toVector.filter(s => !v1.contains(s))
    val v3 = v1 ++ v2
    val v4 = that.unmatched.map(i => that.songs(i)).toVector.filter(s => !v3.contains(s))
    val v5 = v3 ++ v4
    val v6 = this.unmatched.map(i => this.songs(i)).toVector.filter(s => !v5.contains(s))
    val newP = new Playlist(v5 ++ v6, f)
    println("XO => new playlist with size " + newP.size)
    newP
  }
  */

}