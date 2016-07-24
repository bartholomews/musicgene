package model.geneticScala

/**
  * SHOULD GIVE SOME POINTS FOR PARTIALLY SUCCESSFUL ONES I GUESS
  * Each Playlist can be assigned a different kind of FitnessCalc
  */
trait FitnessFunction {
  def getFitness(playlist: Playlist): Float
}
