package model.geneticScala

import model.constraints.{IncludeSmaller, _}
import model.music._

import scala.annotation.tailrec

/**
  * @see Akka version
  *      https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
  * Actually, evolve() could be an asynchronous message, so that also the server can do other stuff.
  */
object GA extends App {

  // ============================================================================

  val startTime = System.currentTimeMillis
  val constraints: Set[Constraint] = Set(
    /*
    UnaryEqualAny(Title("Tha")),
    Include(0, Artist("Aphex Twin")),
    Include(1, Artist("Aphex Twin")),
    Include(2, Artist("Aphex Twin")),
    Include(3, Title("Brooklyn Zoo")),
    Include(4, Title("Method Man")),
    */
    // no song with tempo 102.397
    //   UnaryEqualNone(Artist("Aphex Twin"))
    // this BinaryLarger will get 0 of course even if it finds 99% of it.
    // maybe if you translate this kinds of attribute into a set of constraints foreach indices it works better
    // e.g.
    //    UnarySmallerNone(Tempo(120))
    // ==, that's right
    IncludeSmaller(0, Tempo(120)),
    IncludeSmaller(1, Tempo(120)),
    IncludeSmaller(2, Tempo(120)),
    IncludeSmaller(3, Tempo(120)),
    IncludeSmaller(4, Tempo(120)),
    IncludeSmaller(5, Tempo(120)),
    IncludeSmaller(6, Tempo(120))
    /*  careful with IndexOutOfBounds
    Include(5, Title("Mistakes")),
    Include(6, Title("Dr. Echt")),
    Include(7, Title("C.R.E.A.M.")),
    Include(8, Title("A Drifting Up")),
    Include(9, Title("#1")),
    Include(10, Title("Come As You Are"))
    */
  )

  // with few songs get stuck, whole database length eventually gets a good score
  val p = generatePlaylist(constraints, 10)
  println("GENERATED PLAYLIST: ")
  p.prettyPrint()

  def generatePlaylist(db: MusicCollection, constraints: Set[Constraint], length: Int): Playlist = {
    if (constraints.isEmpty) {
      println("NO CONSTRAINTS!")
      generateRandomPlaylist(db, length)
    } else {
      val pop = PopFactory.generatePopulation(db, StandardFitness(constraints), length)
      evolve(pop, 1)
    }
  }

  def generatePlaylist(constraints: Set[Constraint], length: Int): Playlist = {
    if (constraints.isEmpty) generateRandomPlaylist(length)
    else {
      val db = new MusicCollection(Cache.extractSongs)
      val pop = PopFactory.generatePopulation(db, StandardFitness(constraints), length)
      evolve(pop, 1)
    }
  }

  def generatePlaylist(db: MusicCollection, constraints: Set[Constraint]): Playlist = {
    if (constraints.isEmpty) generateRandomPlaylist(db)
    else {
      val pop = PopFactory.generatePopulation(db, StandardFitness(constraints))
      evolve(pop, 1)
    }
  }

  def generatePlaylist(constraints: Set[Constraint]): Playlist = {
    if (constraints.isEmpty) generateRandomPlaylist()
    else {
      val db = new MusicCollection(Cache.extractSongs)
      val pop = PopFactory.generatePopulation(db, StandardFitness(constraints))
      evolve(pop, 1)
    }
  }

  // i still feel playlists shouldnt have a fitnessfunc
  def generateRandomPlaylist(length: Int) = {
    new Playlist(util.Random.shuffle(Cache.extractSongs).take(length), NoFitness())
  }

  def generateRandomPlaylist(db: MusicCollection) = {
    new Playlist(util.Random.shuffle(db.songs).take(20), NoFitness())
  }

  def generateRandomPlaylist(db: MusicCollection, length: Int) = {
    println("OK, JUST A RANDOM P-LIST THEM")
    new Playlist(util.Random.shuffle(db.songs).take(length), NoFitness())
  }

  def generateRandomPlaylist() = {
    new Playlist(util.Random.shuffle(Cache.extractSongs).take(20), NoFitness())
  }

  @tailrec
  private def evolve(pop: Population, generation: Int): Playlist = {
    printGAResults(pop, generation)
    if (generation >= GASettings.maxGen || pop.maxFitness >= GASettings.maxFitness) pop.getFittest
    else evolve(pop.evolve, generation + 1)
  }

  private def printGAResults(pop: Population, generation: Int): Unit = {
    println("=" * 20 + "GEN-" + generation + "=" * 20)
    println("GENERATION " + generation + ", max fitness: " + pop.maxFitness)
    println("FITTEST:")
    pop.getFittest.prettyPrint()
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