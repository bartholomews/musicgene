package model.geneticScala

import model.music.MusicCollection

/**
  * Created by mba13 on 04/07/2016.
  */
object PopFactory {

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
    new Population(PlaylistsFactory.generatePlaylists(db, GASettings.popSize, size)
      .sortWith((p1, p2) => f.getFitness(p1) < f.getFitness(p2)), f)
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
    new Population(
      PlaylistsFactory.generatePlaylists(db, GASettings.popSize, f)
        .sortWith((p1, p2) => f.getFitness(p1) < f.getFitness(p2)), f)
  }

}
