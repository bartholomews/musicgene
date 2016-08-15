package model.geneticScala

import model.constraints.{Constraint, Score, UnaryConstraint}


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
/*
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
    * @return
    * NaN for 0.0/0.0
    */
  /*
  override def getFitness(playlist: Playlist): Double = {
    val f1 = constraints.count(constraint => constraint.score(playlist))
    val f2 = constraints.size.toDouble
    val f = constraints.count(constraint => constraint.score(playlist)) / constraints.size.toDouble
    val r = BigDecimal.decimal(f).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    println(f1 + " / " + f2 + " = " + r)
    r
  }
  */
  * }
  * */

case class CostBasedFitness(constraints: Set[Constraint]) extends FitnessFunction {
  override def getConstraints: Set[Constraint] = constraints.asInstanceOf[Set[Constraint]]

  override def score(p: Playlist): Set[Score] = constraints.flatMap(c => c.score(p))

  override def getFitness(p: Playlist): Double = {
    if(constraints.isEmpty) 0.0
    else {
      val f1 = score(p).count(s => s.matched)
      val f2 = score(p).size.toDouble
      val f = score(p).count(s => s.matched) / score(p).size.toDouble
      BigDecimal.decimal(f).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  //    println(f1 + " / " + f2 + " = " + r + " (distance: " + getDistance(p) + ")")
    }
  }
  override def getDistance(p: Playlist): Double = {
    val distance = score(p).map(s => s.distance).sum
    BigDecimal.decimal(distance).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}

// ???
  /*
case class NoFitness() extends FitnessFunction {
  override def getFitness(p: Playlist) = throw new Exception("NoFitness.getFitness")
}
*/