package model.geneticScala

import model.constraints.{Include, IncludeAny, _}
import model.music._

/**
  * @see Akka version
  *      https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
  */
object GA extends App {

  // ============================================================================

  val startTime = System.currentTimeMillis

  val db = new MusicCollection(Cache.extractSongs)
  val constraints: Set[Constraint] = Set(
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

  // Create the initial population
  var pop = PopFactory.generatePopulation(db, new StandardFitness(constraints))

  val maxFitness = 1.0

  // Start evolving the population, stopping when the maximum number of
  // generations is reached, or when we find a solution.
  var generation = 0
  println("Initial POP: ")
  pop.prettyPrint()
  println("-----------F-I-T-T-E-S-T----------------")
  println(pop.getFittest.prettyPrint())
  println("----------------------------------------")

  while(generation < GASettings.maxGen && pop.maxFitness < maxFitness) {
    generation += 1
    println("----------------------------------------")
    println("EVOLUTION " + generation)
    pop = pop.evolve
    val f = pop.getFittest
    println("Best fitness: " + pop.maxFitness)
    println("Max fitness: " + maxFitness)
    // pop.getFittest.prettyPrint()
  }

  println("FINAL POP:")
  pop.getFittest.prettyPrint()

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
