package model.genetic

import model.constraints.{Constraint, Score}

/**
  * Trait to collect the Score values from a Playlist over a Set of Constraints
  */
trait FitnessFunction {
  val constraints: Set[Constraint]
  def score(playlist: Playlist): Seq[Score] = constraints.toSeq.flatMap(c => c.score(playlist))
  def getFitness(playlist: Playlist): Double
  def getDistance(playlist: Playlist): Double = 0.0
}

/**
  * Calculate fitness as a decimal percentage value rounded to the nearest tenth
  *
  * @param constraints the Set of Constraints to evaluate over a Playlist
  */
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

}