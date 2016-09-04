package model.genetic

import model.music.MusicCollection

/**
  * Created by mba13 on 04/07/2016.
  */
object PopFactory {

  def sortByFitness(p: Population) = {
    val byFitness = p.playlists.sortWith((p1, p2) => p1.fitness > p2.fitness)
    val maxFitness = byFitness.head.fitness
     // println("MAXFITNESS: " + maxFitness)
    val (elites, inferiors) = byFitness.partition(p => p.fitness == maxFitness)

    /*
    println("=========")
    println("ELITES: ")
    elites.foreach(e => println(e.fitness))
    println("INFERIORS:")
    inferiors.foreach(i => println(i.fitness))
    println("=========")
    */

    //  newPOP.playlists.foreach(p => println(p.fitness)
    //new Population(p.playlists.sortWith((p1, p2) => p1.distance < p2.distance))
    new Population(elites.sortBy(p => p.distance) ++ inferiors)

    //new Population(elites.sortWith((p1, p2) => p1.distance < p2.distance) ++ inferiors)
  }

  /**
    * Generate a Population of `popSize` playlists each containing `size` songs from the db collection
    * in random order
    *
    * @param db
    * @param f
    * @param size
    * @return
    */
  def generatePopulation(db: MusicCollection, f: FitnessFunction, size: Int) = {
    sortByFitness(
      new Population(PlaylistsFactory.generatePlaylists(db, GASettings.popSize, size, f))
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
      PlaylistsFactory.generatePlaylists(db, GASettings.popSize, f)
    ))
  }

  def generateUniquePopulation(db: MusicCollection, f: FitnessFunction, size: Int) = {
    sortByFitness(new Population(
      PlaylistsFactory.generateUniquePlaylists(db, GASettings.popSize, size, f)
    ))
  }

}
