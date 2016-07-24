package model.geneticScala

import model.constraints.Constraint

/**
* Created by mba13 on 24/07/2016.
*/
case class StandardFitness(constraints: Set[Constraint]) extends FitnessFunction {

/**
  * Fitness function defined as the number of matched constraints in a Playlist,
  * as a decimal percentage rounded to the nearest hundredth.
  *
  * TODO some points for partially successful ones?
  *      how to deal with constraint relative to relationship between tracks?
  *
  * @param playlist the Playlist to be tested against the constraints
  * @return
  */
override def getFitness(playlist: Playlist): Float = {
  val f = constraints.count(constraint => constraint.calc(playlist)) / constraints.size.toFloat
  BigDecimal.decimal(f).setScale(2, BigDecimal.RoundingMode.HALF_UP).toFloat
}

}
