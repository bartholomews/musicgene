package model.geneticScala

import model.constraints.{Constraint, Score}
import model.music.Attribute


/**
  * SHOULD GIVE SOME POINTS FOR PARTIALLY SUCCESSFUL ONES I GUESS
  * Each Playlist can be assigned a different kind of FitnessCalc
  */
trait FitnessFunction {
  val constraints: Set[Constraint]
  def score(playlist: Playlist): Seq[Score] = constraints.toSeq.flatMap(c => c.score(playlist))
  def getFitness(playlist: Playlist): Double
  def getDistance(playlist: Playlist): Double
  def mapping(p: Playlist): Map[Attribute, Seq[(Int, Double)]]
}


/**
 *
*/
/*
case class StandardFitness(constraints: Set[UnaryConstraint]) extends FitnessFunction {

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
  override def getFitness(p: Playlist): Double = {
    if(constraints.isEmpty) 1.0
    else {
      val playlistScore = score(p)
      val f = playlistScore.count(s => s.matched) / playlistScore.size.toDouble
      BigDecimal.decimal(f).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
  }
  override def getDistance(playlist: Playlist): Double = {
    val distance = score(playlist).flatMap(s => s.info).map(i => i.distance).sum
    BigDecimal.decimal(distance).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  // TODO
  override def mapping(p: Playlist): Map[Attribute, Seq[(Int, Double)]] = {
    p.scores.flatMap(scores => scores.info)
      .groupBy(info => info.attr)
      // m: Map[Attribute, Set[MonotonicInfo]] mapped into Attribute -> Set[(Int, Double)]
      .map(m => m._1 -> m._2.map(i => (i.index, i.distance)))
  }
}