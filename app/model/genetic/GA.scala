package model.genetic

import model.constraints.Constraint
import model.music._

import scala.annotation.tailrec

/**
  * Main object which starts the genetic algorithm procedure
  */
object GA {

  def generatePlaylist(db: MusicCollection, c: Set[Constraint], length: Int): Playlist = {
    GA.generatePlaylist(db, CostBasedFitness(c), length)
  }

  def generatePlaylist(db: MusicCollection, f: FitnessFunction, length: Int): Playlist = {
    if (f.constraints.isEmpty) generateRandomPlaylist(db, length, f)
    else {
      val pop = PopFactory.generatePopulation(db, f, length)
      evolve(pop, 1)
    }
  }

  def generateRandomPlaylist(db: MusicCollection, length: Int, f: FitnessFunction): Playlist = {
    new Playlist(scala.util.Random.shuffle(db.songs).take(length), f)
  }

  /**
    * Genetic algorithm entry point
    *
    * @param pop the current Population of candidate Playlists
    * @param generation the current generation, incremented for each recursion
    * @return the fittest playlist if reached max fitness value or max number of generations,
    *         otherwise it will recursively evolve itself over a new generation
    *         to generate a fitter population
    */
  @tailrec
  private def evolve(pop: Population, generation: Int): Playlist = {
    if (generation >= GASettings.maxGen || pop.maxFitness >= GASettings.maxFitness) {
      pop.fittest
    }
    else evolve(pop.evolve, generation + 1)
  }

}