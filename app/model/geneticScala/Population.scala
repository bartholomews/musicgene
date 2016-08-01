package model.geneticScala

import model.music.MusicCollection

import scala.util.Random

/**
  * A Population of `popSize` playlists each containing a random sequence
  * of `size` songs in the Music Collection.
  * TODO this means the MC has to be filtered before running this.
  * The collection might have duplicates, to avoid that need another function???
  * I don't think so
  *
  * playlists should be already sorted with fitness function, with head having the best fitness score
  */
class Population(val playlists: Vector[Playlist], f: FitnessFunction) {

  // the number of the initial candidate playlists
  val popSize = playlists.length
  val size = playlists(0).size

  def get(i: Int) = playlists(i)
  def apply(c: Playlist) = playlists.find(x => x == c)

  def getFitness(p: Playlist): Double = f.getFitness(p)
  def getFittest: Playlist = playlists.head //playlists.sortWith((p1, p2) => getFitness(p1) < getFitness(p2)).head // playlists.reduce(fittest)
  def fittest(x: Playlist, y: Playlist): Playlist = if(getFitness(x) < getFitness(y)) x else y
  val maxFitness = getFitness(getFittest)

  /*
  // since it's sorted, would be just smaller indices
  // why with this group it bounces back and forth? I think crossover
  val getFittest = playlists.head
  def fittest(p1: Playlist, p2: Playlist) = if(f.getFitness(p1) >= f.getFitness(p2)) p1 else p2
  def getFitness(c: Playlist) = f.getFitness(c)
  val maxFitness = f.getFitness(playlists.head)
  */


  def prettyPrint() = {
    playlists.foreach(p => {
      println("=" * 10 + '\n' + "PLAYLIST " + playlists.indexOf(p) + " (" + getFitness(p) + ")" + '\n' + "=" * 10)
      p.prettyPrint()
    })
  }

  /**
    * Evolution version:
    * http://www.theprojectspot.com/tutorial-post/applying-a-genetic-algorithm-to-the-travelling-salesman-problem/5
    *
    */
  def evolve: Population = {
    // https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
    val elites = scala.math.round(popSize * GASettings.elitismRatio)
    val eliteBuffer: Vector[Playlist] = playlists.take(elites) // what for?

    /*
    println("ELITES:")
    eliteBuffer.foreach(p => p.prettyPrint())
    println("==============================")
    */

    // jsvazic uses Futures and Akka Router
    val inferiors: Array[Playlist] = (for (i <- elites until popSize) yield {
      // double check immutability with these arrays
      val darwinian = playlists(i)
      if (Random.nextFloat <= GASettings.crossoverRatio) {
        crossover(darwinian, eliteBuffer(Random.nextInt(eliteBuffer.length)))
      }
      else mutate(darwinian)
    }).toArray

    val p = new Population((eliteBuffer ++ inferiors).sortWith((p1, p2) => f.getFitness(p1) < f.getFitness(p2)), f)
    //.sortBy(p => getFitness(p)), f)
    // why not use PopFactory?

    //println("NEW POP:")
    //p.prettyPrint()
    p
  }

  // will mutate also fit ones, if over there it will test each song to be mutated:
  // that is, this is a high mutation rate algo
  def mutate(p: Playlist): Playlist = p.mutate

  /*
  private def randomMutate(p: Playlist) = {
    if (Random.nextFloat() <= GASettings.mutationRatio) p.mutate else p
  }
  */

  /**
    * Single point crossover: permutation is copied from the first parent
    * until the crossover point, then the other parent is scanned and if
    * the song is not yet in the offspring, it is added;
    * TODO this will work if the two playlists have the same size and set
    * of songs.
    */
  def crossover(p1: Playlist, p2: Playlist): Playlist = {
    if(fittest(p1, p2) == p1) p1.crossover(p2)
    else p2.crossover(p1)
  }


}