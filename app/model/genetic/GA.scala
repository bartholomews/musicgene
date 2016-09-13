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

  def generatePlaylist(db: MusicCollection, c: Set[Constraint], length: Int): (Playlist, Option[GAResponse]) = {
    GA.generatePlaylist(db, CostBasedFitness(c), length)
  }

  // ============================================================================

  def generatePlaylist(db: MusicCollection, f: FitnessFunction, length: Int): (Playlist, Option[GAResponse]) = {
//    val start: Long = System.currentTimeMillis()
    if (f.constraints.isEmpty) generateRandomPlaylist(db, length, f)
    else {
      val pop = PopFactory.generatePopulation(db, f, length)
//      val end: Long = System.currentTimeMillis()
//      println("=" * 50)
//      println("INITIAL POPULATION CREATED IN " + (end - start) + " ms")
//      val start2: Long = System.currentTimeMillis()
      val p = evolve(pop, 1)
//      val end2: Long = System.currentTimeMillis()
//      val watch: Long = end2 - start2
//      println("=" * 50)
//      println("SOLUTION FOUND IN " + watch + " ms")
//      println("=" * 50)
      p
    }
  }

  def generateRandomPlaylist(db: MusicCollection, length: Int, f: FitnessFunction): (Playlist, Option[GAResponse]) = {
    (new Playlist(scala.util.Random.shuffle(db.songs).take(length), f), None)
  }

  /**
    * TODO stop after 10 secs
    *
    * @param pop
    * @param generation
    * @param GAStatistics
    * @return
    */
  @tailrec
  private def evolve(pop: Population, generation: Int, GAStatistics: Boolean = false): (Playlist, Option[GAResponse]) = {
    val start: Long = System.currentTimeMillis()
    val response = GAResponse(generation, pop.maxFitness, pop.minDistance, pop.fittest.unmatchedIndexes.map(i => i))
    response.prettyPrint()
   // val matchedWorst = pop.fittest.matchedWorst
   // print("WORST MATCHED INT: " + matchedWorst)
   // if (matchedWorst.isDefined) println(" with distance " + pop.fittest.distance(pop.fittest.matchedWorst.get))

//    val end: Long = System.currentTimeMillis()
//    val time: Long = end - start

    if (generation >= GASettings.maxGen
      || pop.maxFitness >= GASettings.maxFitness) {
      if (GAStatistics) (pop.fittest, Some(response))
      else {
//        println(time + " ms")
        // response.unmatched.foreach(i => print(i) + " ")
        (pop.fittest, None)
      }
    }
    else {
      if (GAStatistics) {
        // sendResponse(response) TODO
        evolve(pop.evolve, generation + 1, GAStatistics)
      }
      else {
//        println(time + " ms")
        evolve(pop.evolve, generation + 1, GAStatistics)
      }
    }
  }

}