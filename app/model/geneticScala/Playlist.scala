package model.geneticScala

import model.constraints.{Score}
import model.music.{Loudness, Song, Tempo, Title}

import scala.util.Random

/**
  *
  */
class Playlist(val songs: Vector[Song], f: FitnessFunction) {

  def get(index: Int) = songs(index)

  val fitness = f.getFitness(this)
  val distance = f.getDistance(this)

  def size = songs.length

  val scores: Seq[Score] = f.score(this)

  val (matched, unmatched) = scores.partition(s => s.matched)

  val matchedIndexes: Set[Int] = {
    matched.flatMap(s => s.info).map(i => i.index).toSet
  }

  val unmatchedIndexes: Set[Int] = {
    unmatched.flatMap(s => s.info).map(i => i.index).toSet
  }

  val matchedWorst: Option[Int] = {
    val m = matched.flatMap(s => s.info)
    if (m.isEmpty) None
    else Some(m.maxBy(i => i.distance).index)
  }

  def distance(i: Int): Option[Double] = scores.flatMap(s => s.info).find(x => x.index == i) match {
    case None => None
    case Some(y) => Some(y.distance)
  }

  //val distanceBucket: Set[Int] = f.mapping(this).map(m => m._1 -> m)

  //def fitness: Float = f.getFitness(this)

  def prettyPrint() = {
    songs.foreach(s => {
      println("- " + s.find(Title("")) + "(T: " + s.find(Tempo(0.0)) + ", L: " + s.find(Loudness(0.0)) + ")")
    })
  }

  // order changing: songs in a playlist are swapped
  // maybe it's cost-effective to just swap 2 songs
  // and move the mutationRatio check in the Population method caller
  def mutate: Playlist = {
    val arr = songs.toArray
    // to avoid local optima and generate new solutions might be better to randomize the thing
    def randomIndex = Random.nextInt(songs.length)
    if(unmatchedIndexes.isEmpty) {  // || Random.nextFloat() < 0.2) {
      // there is no index information for the unmatched constraints,
      // do random swap mutation // (also a small chance to go random regardless, to improve novelty and new solutions)
      val v1 = randomIndex
      val v2 = randomIndex
      val aux = arr(v1)
      arr(v1) = arr(v2)
      arr(v2) = aux
    } else {
      // shuffle the unmatched indexes
      val weakBucket = Random.shuffle(unmatchedIndexes)
      // each unmatched index might be swapped with another random index of the playlist
      for (weakIndex <- weakBucket) {
        if (Random.nextFloat() < GASettings.mutationRatio) {
          // the random index is any unmatched (doesn't need to have an index value, i.e. in unmatchedIndexes Set,
          // as it might belong to a different Score case class which doesn't return indexes)
          //val randomIndex = Random.shuffle(songs.indices.filterNot(i => matchedIndexes.contains(i))).head
          val v1 = randomIndex
          val v2 = arr(weakIndex)
          arr(weakIndex) = arr(v1)
          arr(v1) = v2
        }
      }
    }
    new Playlist(arr.toVector, f)

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

  /**
    * Single-point crossover:
    * one crossover point is selected, the permutation is copied
    * from the first parent until the crossover point,
    * then the other parent is scanned and if the value is
    * not yet in the offspring, it is added.
    * TODO investigate on more refined ways to produce xo
    *
    * @param that
    * @return
    */
  def crossover(that: Playlist) = {
    val pivot = Random.nextInt(songs.length)
    val v1 = this.songs.take(pivot)
    val v2 = that.songs.filter(s => !v1.contains(s)).take(that.songs.length - pivot)
    new Playlist(v1 ++ v2, f)
  }

  /*
  CROSSOVER VERSION 2: it reaches to local optima way too soon
  Take the indexes matched of inferior playlist
  add the indexes matched of superior playlist
  add the indexes unmatched of inferior playlist until right size is reached
  add the indexes unmatched of superior playlist until right size is reached
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