package model.geneticScala

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

  def generatePlaylist(db: MusicCollection, f: FitnessFunction, length: Int): Playlist = {
    if (f.getConstraints.isEmpty) {
      generateRandomPlaylist(db, length, f)
    } else {
      val pop = PopFactory.generatePopulation(db, f, length)
      evolve(pop, 1)
    }
  }

  // ============================================================================

  def generatePlaylist(f: FitnessFunction, length: Int): Playlist = {
    if (f.getConstraints.isEmpty) generateRandomPlaylist(length, f)
    else {
      val db = new MusicCollection(Cache.extractSongs)
      val pop = PopFactory.generatePopulation(db, f, length)
      evolve(pop, 1)
    }
  }

  def generatePlaylist(db: MusicCollection, f: FitnessFunction): Playlist = {
    if (f.getConstraints.isEmpty) generateRandomPlaylist(db, f)
    else {
      val pop = PopFactory.generatePopulation(db, f)
      evolve(pop, 1)
    }
  }

  def generatePlaylist(f: FitnessFunction): Playlist = {
    if (f.getConstraints.isEmpty) generateRandomPlaylist(f)
    else {
      val db = new MusicCollection(Cache.extractSongs)
      val pop = PopFactory.generatePopulation(db, f)
      evolve(pop, 1)
    }
  }

  // i still feel playlists shouldnt have a fitnessfunc
  def generateRandomPlaylist(length: Int, f: FitnessFunction) = {
    new Playlist(util.Random.shuffle(Cache.extractSongs).take(length), f)
  }

  def generateRandomPlaylist(db: MusicCollection, f: FitnessFunction) = {
    new Playlist(util.Random.shuffle(db.songs).take(20), f)
  }

  def generateRandomPlaylist(db: MusicCollection, length: Int, f: FitnessFunction) = {
    println("OK, JUST A RANDOM P-LIST THEM")
    val p = new Playlist(util.Random.shuffle(db.songs).take(length), f)
    p
  }

  def generateRandomPlaylist(f: FitnessFunction) = {
    new Playlist(util.Random.shuffle(Cache.extractSongs).take(20), f)
  }

  @tailrec
  private def evolve(pop: Population, generation: Int): Playlist = {
    printGAResults(pop, generation)
    if (generation >= GASettings.maxGen || pop.maxFitness >= GASettings.maxFitness) pop.fittest
    else evolve(pop.evolve, generation + 1)
  }

  private def printGAResults(pop: Population, generation: Int): Unit = {
    println("=" * 20 + "GEN-" + generation + "=" * 20)
    println("GENERATION " + generation + ", max fitness: " + pop.maxFitness + ", distance: " + pop.minDistance +
              ", unmatched: " + pop.fittest.unmatched.map(i => i + 1).toString())
  //  println("FITTEST:")
  //  pop.fittest.prettyPrint()
  }

}

/*

  */
    /*
    println("-=-=-=-=-=-=-=-=-==-==-==-=")
    var n = 0
    for (p <- pop.playlists) {
        println("PLAYLIST " + n + " (fitness: " + pop.maxFitness)
   //   p.prettyPrint()
      println("-------------------------")
      n = n + 1
    }
    println("-=-=-=-=-=-=-=-=-==-==-==-=")
    */
/*
    println("=" * 48)

*/


  /*
  def iterative() {
    // Create the initial population
    var pop = PopFactory.generatePopulation(db, StandardFitness(constraints))

    // Start evolving the population, stopping when the maximum number of
    // generations is reached, or when we find a solution.
    var generation = 0
    println("Initial POP: ")
    pop.prettyPrint()
    println("-----------F-I-T-T-E-S-T----------------")
    println(pop.maxFitness)
    println("----------------------------------------")

    while (generation < GASettings.maxGen && pop.maxFitness < GASettings.maxFitness) {
      generation += 1
      println("----------------------------------------")
      println("EVOLUTION " + generation)
      pop = pop.evolve
      val f = pop.playlists.head
      println("Best fitness: " + pop.maxFitness)
      println("Max fitness: " + GASettings.maxFitness)
      // pop.getFittest.prettyPrint()
    }

    println("FINAL POP:")
    pop.playlists.head.prettyPrint()

    /*
  while (generation <= GASettings.maxGen && pop.maxFitness < maxFitness) {
    generation += 1
    println("Generation " + generation + ": ")
    val newPop = pop.evolve
    println("=======================")
    newPop.prettyPrint()
    newPop.getFittest.prettyPrint()
    println(" (" + newPop.maxFitness + ")")
  }
  */

    /*
  // We're done, so shutdown the population (which uses Akka)
  pop.shutdown
  */

    val endTime = System.currentTimeMillis

    // TODO sort by fitness and get 0 instead of searching
    //println("Generation " + generation + "; fittest: " + pop.getFittest.toString)
    println("Total execution time: " + (endTime - startTime) + "ms")

  }

}


/*
def generatePlaylist(ids: Vector[String], constraints: Set[Constraint], size: Int): Vector[Song] = {
  if(constraints.isEmpty) util.Random.shuffle(Cache.extractSongs(ids)).take(size)
  else {
    val db = new MusicCollection(Cache.extractSongs(ids))
    val pop = PopFactory.generatePopulation(db, StandardFitness(constraints), size)
    evolve(pop, 1).songs
  }
}
*/

*/