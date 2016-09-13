package model.genetic

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

  def distance(n: Int): Option[Double] = scores.flatMap(s => s.info).find(i => i.index == n) match {
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

  def randomIndex = Random.nextInt(songs.length)

  // order changing: songs in a playlist are swapped
  // maybe it's cost-effective to just swap 2 songs
  // and move the mutationRatio check in the Population method caller

  /**
    *
    * @return
    */
  def mutate: Playlist = {
    val arr = songs.toArray
    if (unmatchedIndexes.isEmpty || Random.nextFloat() < 0.1) { new Playlist(randomSwapMutation(arr), f) }
    else { new Playlist(indexedMutation(arr), f) }
  }

  def randomSwapMutation(arr: Array[Song]): Vector[Song] = {
    val v1 = randomIndex
    val v2 = randomIndex
    val aux = arr(v1)
    arr(v1) = arr(v2)
    arr(v2) = aux
    arr.toVector
  }

  def indexedMutation(arr: Array[Song]): Vector[Song] = {
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
    arr.toVector
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
  def crossover(that: Playlist): Playlist = {
    val pivot = Random.nextInt(songs.length)
    val v1 = this.songs.take(pivot)
    val v2 = that.songs.filter(s => !v1.contains(s)).take(that.songs.length - pivot)
    new Playlist(v1 ++ v2, f)
  }

  /*
  CROSSOVER VERSION 2: it reaches to local optima way too soon, well actually it explodes
  because it creates different size playlists!!!!!!
  Take the indexes matched of inferior playlist
  add the indexes matched of superior playlist
  add the indexes unmatched of inferior playlist until right size is reached
  add the indexes unmatched of superior playlist until right size is reached
 */
  def crossoverV2(that: Playlist) = {
    val v1 = getSongsAtIndex(that, matchedIndexes)
    val v2 = getSongsAtIndex(this, matchedIndexes).filter(s => !v1.contains(s))
    val v3 = (v1 ++ v2).take(songs.length)
    if(v3.length < songs.length) {
      val v4 = Random.shuffle(
        getSongsAtIndex(that, unmatchedIndexes) ++ getSongsAtIndex(this, unmatchedIndexes)
      ).take(songs.length - v3.length)
      new Playlist(v3 ++ v4, f)
    }
    else new Playlist(v3, f)
    //println("XO => new playlist with size " + newP.size)
  }

  def getSongsAtIndex(p: Playlist, indexes: Set[Int]): Vector[Song] = {
    indexes.map(i => p.songs(i)).toVector
  }

}