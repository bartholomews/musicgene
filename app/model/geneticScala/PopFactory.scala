package model.geneticScala

import model.music.MusicCollection

/**
  * Created by mba13 on 04/07/2016.
  */
object PopFactory {

  def sortByFitness(p: Population) = {
    val byFitness = p.playlists.sortWith((p1, p2) => p1.fitness > p2.fitness)
    val maxFitness = byFitness.head.fitness
    val (elites, inferiors) = byFitness.partition(p => p.fitness != maxFitness)
    new Population(elites.sortWith((p1, p2) => p1.distance < p2.distance) ++ inferiors, p.f)
  }

  /**
    * Generate a Population of `popSize` playlists each containing `size` songs from the db collection
    * in random order
    *
    * @param db
    * @param f
    * @param size
    * @return TODO remove f from Pop, is ok in Pl
    */
  def generatePopulation(db: MusicCollection, f: FitnessFunction, size: Int) = {
    sortByFitness(
      new Population(PlaylistsFactory.generatePlaylists(db, GASettings.popSize, size, f), f)
    )
  }

  /**
    * Generate a Population of `popSize` playlists each containing the whole db collection
    * in random order
    *
    * @param db
    * @param f
    * @return
    */
  def generatePopulation(db: MusicCollection, f: FitnessFunction) = {
    sortByFitness(new Population(
      PlaylistsFactory.generatePlaylists(db, GASettings.popSize, f), f))
  }

  def generateUniquePopulation(db: MusicCollection, f: FitnessFunction, size: Int) = {
    sortByFitness(new Population(
      PlaylistsFactory.generateUniquePlaylists(db, GASettings.popSize, size, f), f))
  }

}
