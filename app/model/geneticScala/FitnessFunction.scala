package model.geneticScala

import model.constraints.{Constraint, Score, ScoreConstraint, UnaryConstraint}


/**
  * SHOULD GIVE SOME POINTS FOR PARTIALLY SUCCESSFUL ONES I GUESS
  * Each Playlist can be assigned a different kind of FitnessCalc
  */
trait FitnessFunction {
  def getConstraints: Set[Constraint]
  def getFitness(playlist: Playlist): Double
  def getDistance(playlist: Playlist): Double
  def score(playlist: Playlist): Set[Score]
}


/**
* Created by mba13 on 24/07/2016.
*/
case class StandardFitness(constraints: Set[UnaryConstraint]) extends FitnessFunction {

  override def getConstraints: Set[Constraint] = constraints.asInstanceOf[Set[Constraint]]

  override def getDistance(playlist: Playlist): Double = throw new Exception("CANNOT GET DISTANCE!")
  override def score(p: Playlist) = throw new Exception("CANNOT GET SCORE!")

  /**
    * Fitness function defined as the number of matched constraints in a Playlist,
    * as a decimal percentage rounded to the nearest hundredth.
    *
    * TODO some points for partially successful ones?
    * how to deal with constraint relative to relationship between tracks?
    *
    * @param playlist the Playlist to be tested against the constraints
    * @return
    * NaN for 0.0/0.0
    */
  override def getFitness(playlist: Playlist): Double = {
    val f = constraints.count(constraint => constraint.calc(playlist)) / constraints.size.toDouble
    BigDecimal.decimal(f).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}

case class CostBasedFitness(constraints: Set[ScoreConstraint]) extends FitnessFunction {
  override def getConstraints: Set[Constraint] = constraints.asInstanceOf[Set[Constraint]]

  override def score(p: Playlist): Set[Score] = constraints.flatMap(c => c.score(p))

  override def getFitness(p: Playlist): Double = {
    val f = score(p).count(s => s.matched) / score(p).size.toDouble
    BigDecimal.decimal(f).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
  override def getDistance(p: Playlist): Double = {
    score(p).map(s => s.distance).sum
  }
}

// ???
  /*
case class NoFitness() extends FitnessFunction {
  override def getFitness(p: Playlist) = throw new Exception("NoFitness.getFitness")
}
*/