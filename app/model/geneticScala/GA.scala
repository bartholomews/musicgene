package model.geneticScala

import model.constraints.Constraint
import model.music._

import scala.annotation.tailrec

/**
  * @see Akka version
  *      https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
  */
object GA extends App {

  // songIDs selected via json or the whole DB (should be only user's) if 'ids' is empty
  def generatePlaylist(ids: Vector[String], constraints: Set[Constraint], size: Int): Vector[Song] = {
    if(constraints.isEmpty) util.Random.shuffle(Cache.extractSongs(ids)).take(size)
    else {
      val db = new MusicCollection(Cache.extractSongs(ids))
      val pop = PopFactory.generatePopulation(db, StandardFitness(constraints), size)
      evolve(pop, 1).songs
    }
  }

  def generatePlaylist(constraints: Set[Constraint]): Playlist = {
    val db = new MusicCollection(Cache.extractSongs)
    val pop = PopFactory.generatePopulation(db, StandardFitness(constraints))
    evolve(pop, 1)
  }

  def generatePlaylist(constraints: Set[Constraint], size: Int): Playlist = {
    val db = new MusicCollection(Cache.extractSongs)
    val pop = PopFactory.generatePopulation(db, StandardFitness(constraints), size)
    evolve(pop, 1)
  }

  @tailrec
  private def evolve(pop: Population, generation: Int): Playlist = {
    println("=" * 20 + "GEN - " + generation + "=" * 20)
    println("GENERATION " + generation + ": " + pop.maxFitness)
    println(pop.getFittest.prettyPrint())
    println("=" * 48)
    if(generation >= GASettings.maxGen || pop.maxFitness >= GASettings.maxFitness) pop.getFittest
    else evolve(pop.evolve, generation + 1)
  }

  // ============================================================================

  val startTime = System.currentTimeMillis


  val db = new MusicCollection(Cache.extractSongs)
  val constraints: Set[Constraint] = Set()
  /*
    IncludeAny(Title("Tha")),
    Include(0, Artist("Aphex Twin")),
    Include(1, Artist("Aphex Twin")),
    Include(2, Artist("Aphex Twin")),
    Include(3, Title("Brooklyn Zoo")),
    Include(4, Title("Method Man")),
    Include(5, Title("Mistakes")),
    Include(6, Title("Dr. Echt")),
    Include(7, Title("C.R.E.A.M.")),
    Include(8, Title("A Drifting Up")),
    Include(9, Title("#1")),
    Include(10, Title("Come As You Are"))
  )
  */

  // Create the initial population
  var pop = PopFactory.generatePopulation(db, new StandardFitness(constraints))

  // Start evolving the population, stopping when the maximum number of
  // generations is reached, or when we find a solution.
  var generation = 0
  println("Initial POP: ")
  pop.prettyPrint()
  println("-----------F-I-T-T-E-S-T----------------")
  println(pop.getFittest.prettyPrint())
  println("----------------------------------------")

  while(generation < GASettings.maxGen && pop.maxFitness < GASettings.maxFitness) {
    generation += 1
    println("----------------------------------------")
    println("EVOLUTION " + generation)
    pop = pop.evolve
    val f = pop.getFittest
    println("Best fitness: " + pop.maxFitness)
    println("Max fitness: " + GASettings.maxFitness)
    // pop.getFittest.prettyPrint()
  }

  println("FINAL POP:")
  pop.getFittest.prettyPrint()

  /*
  // We're done, so shutdown the population (which uses Akka)
  pop.shutdown
  */

  val endTime = System.currentTimeMillis

  // TODO sort by fitness and get 0 instead of searching
  //println("Generation " + generation + "; fittest: " + pop.getFittest.toString)
  println("Total execution time: " + (endTime - startTime) + "ms")

}