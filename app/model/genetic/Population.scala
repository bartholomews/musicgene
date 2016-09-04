package model.genetic

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

/**
  * @see John Svazic's GAHelloWorld
  *
  * SELECTION
  * select the best fit from the population in each generation.
  * The fitness is the number of model.constraints satisfied by it.
  *
  * CROSSOVER
  * create new solutions exchanging the gene information between two solutions
  * to generate a new one.
  *
  * MUTATION
  * introduce new features into the population pool after crossover,
  * to maintain diversity.
  *
  */

// encoding should be Permutation
// @see https://courses.cs.washington.edu/courses/cse473/06sp/GeneticAlgDemo/encoding.html
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

  /*
  // since it's sorted, would be just smaller indices
  // why with this group it bounces back and forth? I think crossover
  val getFittest = playlists.head
  def fittest(p1: Playlist, p2: Playlist) = if(f.getFitness(p1) >= f.getFitness(p2)) p1 else p2
  def getFitness(c: Playlist) = f.getFitness(c)
  val maxFitness = f.getFitness(playlists.head)
  */

  /**
    * Evolution version:
    * http://www.theprojectspot.com/tutorial-post/applying-a-genetic-algorithm-to-the-travelling-salesman-problem/5
    * https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
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