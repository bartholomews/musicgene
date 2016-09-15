package model.genetic

/**
  *
  */
case class GAResponse(generation: Int, fitness: Double, distance: Double, unmatched: Set[Int]) {
  val maxGen = GASettings.maxGen
  val maxFitness = GASettings.maxFitness

  def prettyPrint() = {
    println("=" * 50)
    println("GENERATION " + generation + ", max fitness: " + fitness + ", distance: " + distance)
      //+ ", unmatched: " + unmatched.toString())
  }

}
