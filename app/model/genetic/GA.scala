package model.genetic

import controllers.MongoController
import model.constraints.Constraint
import model.music._

import scala.annotation.tailrec

/**
  * @see Akka version
  *      https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
  * Actually, evolve() could be an asynchronous message, so that also the server can do other stuff.
  */
object GA {

  // ============================================================================

  val startTime = System.currentTimeMillis

  def generatePlaylist(db: MusicCollection, c: Set[Constraint], length: Int): (Playlist, Option[GAResponse]) = {
    if (db.songs.isEmpty) GA.generatePlaylist(new MusicCollection(MongoController.readAll), CostBasedFitness(c), length)
    else GA.generatePlaylist(db, CostBasedFitness(c), length)
  }

  def generatePlaylist(db: MusicCollection, f: FitnessFunction, length: Int, GAStatistics: Boolean = false): (Playlist, Option[GAResponse]) = {
    generatePlaylist(db, f, length)
  }

  // ============================================================================

  def generatePlaylist(db: MusicCollection, f: FitnessFunction, length: Int): (Playlist, Option[GAResponse]) = {
    if (f.constraints.isEmpty) generateRandomPlaylist(db, length, f)
    else {
      val pop = PopFactory.generatePopulation(db, f, length)
      evolve(pop, 1, time = 0.0.toLong)
    }
  }

  def generateRandomPlaylist(db: MusicCollection, f: FitnessFunction): (Playlist, Option[GAResponse]) = {
    (new Playlist(scala.util.Random.shuffle(db.songs).take(20), f), None)
  }

  def generateRandomPlaylist(db: MusicCollection, length: Int, f: FitnessFunction): (Playlist, Option[GAResponse]) = {
    (new Playlist(scala.util.Random.shuffle(db.songs).take(length), f), None)
  }

  def generateRandomPlaylist(f: FitnessFunction): (Playlist, Option[GAResponse]) = {
    (new Playlist(scala.util.Random.shuffle(MongoController.readAll).take(20), f), None)
  }

  /**
    * TODO stop after 10 secs
    *
    * @param pop
    * @param generation
    * @param GAStatistics
    * @param time
    * @return
    */
  @tailrec
  private def evolve(pop: Population, generation: Int, GAStatistics: Boolean = false, time: Long): (Playlist, Option[GAResponse]) = {
    println("TIME: " + time)
    val start = System.nanoTime()
    val response = GAResponse(generation, pop.maxFitness, pop.minDistance, pop.fittest.unmatchedIndexes.map(i => i))
    response.prettyPrint()
    val matchedWorst = pop.fittest.matchedWorst
    print("WORST MATCHED INT: " + matchedWorst)
    if (matchedWorst.isDefined) println(" with distance " + pop.fittest.distance(pop.fittest.matchedWorst.get))

    if (generation >= GASettings.maxGen
      || pop.maxFitness >= GASettings.maxFitness) {
      if (GAStatistics) (pop.fittest, Some(response))
      else (pop.fittest, None)
    }
    else {
      val end = System.nanoTime()
      val watch = end - start
      if (GAStatistics) {
        // sendResponse(response) TODO
        evolve(pop.evolve, generation + 1, GAStatistics, time + watch)
      }
      else evolve(pop.evolve, generation + 1, GAStatistics, time + watch)
    }
  }

}