package model.genetic

import model.constraints.Score
import model.music.Song

import scala.util.Random

/**
 * A candidate playlist which gets evaluated over a fitness function
 */
class Playlist(val songs: List[Song], f: FitnessFunction) {

  def get(index: Int) = songs(index)

  val fitness = f.getFitness(this)
  val distance = f.getDistance(this)

  def size = songs.length

  val scores: Seq[Score] = f.score(this)
  val (matched, unmatched) = scores.partition(s => s.matched)
  val matchedIndexes: Set[Int] = {
    matched.flatMap(s => s.info).map(i => i.index).toSet
  }

  /**
   * A Set of optional information about the unmatched indexes of this playlist
   */
  val unmatchedIndexes: Set[Int] = {
    unmatched.flatMap(s => s.info).map(i => i.index).toSet
  }

  /**
   * The matched index with worst distance.
   */
  val matchedWorst: Option[Int] = {
    val m = matched.flatMap(s => s.info)
    if (m.isEmpty) None
    else Some(m.maxBy(i => i.distance).index)
  }

  /**
   * @param n the index to retrieve
   * @return the distance result for the index n on this playlist
   */
  def distance(n: Int): Option[Double] = scores.flatMap(s => s.info).find(i => i.index == n) match {
    case None    => None
    case Some(y) => Some(y.distance)
  }

  def randomIndex = Random.nextInt(songs.length)

  def mutate: Playlist = {
    val arr = songs.toArray
    if (unmatchedIndexes.isEmpty || Random.nextFloat() < 0.1) {
      new Playlist(randomSwapMutation(arr), f)
    } else {
      new Playlist(indexedMutation(arr), f)
    }
  }

  def randomSwapMutation(arr: Array[Song]): List[Song] = {
    val v1 = randomIndex
    val v2 = randomIndex
    val aux = arr(v1)
    arr(v1) = arr(v2)
    arr(v2) = aux
    arr.toList
  }

  def indexedMutation(arr: Array[Song]): List[Song] = {
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
    arr.toList
  }

  /**
   * Single-point crossover:
   * one crossover point is selected, the permutation is copied
   * from the first parent until the crossover point,
   * then the other parent is scanned and if the value is
   * not yet in the offspring, it is added.
   *
   */
  def crossover(that: Playlist): Playlist = {
    val pivot = Random.nextInt(songs.length)
    val v1 = this.songs.take(pivot)
    val v2 = that.songs.filter(s => !v1.contains(s)).take(that.songs.length - pivot)
    new Playlist(v1 ++ v2, f)
  }

  def getSongsAtIndex(p: Playlist, indexes: Set[Int]): Vector[Song] =
    indexes.map(i => p.songs(i)).toVector

}
