package io.bartholomews.musicgene.model.genetic

import scala.util.Random

/**
  * A Population of `popSize` playlists each containing a random sequence
  * of `size` songs in the Music Collection.
  * @see https://courses.cs.washington.edu/courses/cse473/06sp/GeneticAlgDemo/encoding.html
  */
class Population(val playlists: Vector[Playlist], t_size: Int = 2) {

  // the number of the initial candidate playlists
  val popSize = playlists.length
  val size = playlists(0).size

  def get(i: Int) = playlists(i)
  def apply(c: Playlist) = playlists.find(x => x == c)

  def fittest(x: Playlist, y: Playlist): Playlist = {
    if(x.fitness == y.fitness) { if(x.distance < y.distance) x else y }
    else if(x.fitness < y.fitness) x
    else y
  }

  val fittest = playlists.head
  val maxFitness = playlists.head.fitness
  val minDistance = playlists.head.distance

  /**
    * @see http://www.theprojectspot.com/tutorial-post/applying-a-genetic-algorithm-to-the-travelling-salesman-problem/5
    * @see https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
    */
  def evolve: Population = {
    val elites = math.round(popSize * GASettings.elitismRatio)
    val eliteBuffer: Vector[Playlist] = playlists.take(elites)

    val offspring: Array[Playlist] = (for (i <- elites until popSize) yield {
      val parent1 = tournament(t_size)
      if(Random.nextFloat <= GASettings.crossoverRatio) {
        val parent2 = tournament(t_size)
        crossover(parent1, parent2)
      }
      else mutate(parent1)
    }).toArray

    PopFactory.sortByFitness(new Population(eliteBuffer ++ offspring))
  }

  def tournament(size: Int = 2): Playlist = {
    (for(i <- 1 to size) yield { playlists(Random.nextInt(popSize)) }).maxBy(p => p.fitness)
  }

  def mutate(p: Playlist): Playlist = p.mutate

  def crossover(p1: Playlist, p2: Playlist): Playlist = {
    if(scala.util.Random.nextInt(2) == 0) p1.crossover(p2)
    else p2.crossover(p1)
  }




}