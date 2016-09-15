package model.genetic

import model.music.MusicCollection

object PopFactory {

  def sortByFitness(p: Population) = {
    val byFitness = p.playlists.sortWith((p1, p2) => p1.fitness > p2.fitness)
    val maxFitness = byFitness.head.fitness
    val (elites, inferiors) = byFitness.partition(p => p.fitness == maxFitness)
    new Population(elites.sortBy(p => p.distance) ++ inferiors)
  }

  /**
    * Generate a Population of `popSize` playlists each containing `size` songs from the db collection
    * in random order
    */
  def generatePopulation(db: MusicCollection, f: FitnessFunction, size: Int) = {
    sortByFitness(new Population(
      PlaylistsFactory.generatePlaylists(db, GASettings.popSize, size, f))
    )
  }

  def generateUniquePopulation(db: MusicCollection, f: FitnessFunction, size: Int) = {
    sortByFitness(new Population(
      PlaylistsFactory.generateUniquePlaylists(db, GASettings.popSize, size, f)
    ))
  }

}
