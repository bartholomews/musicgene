package model.genetic

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
  def mapping(p: Playlist): Map[Attribute, Seq[(Int, Double)]]
  def getDistance(playlist: Playlist): Double
}

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
      // m: Map[Attribute, Set[Info]] mapped into Attribute -> Set[(Int, Double)]
          .map(m => m._1 -> m._2.map(i => (i.index, i.distance)))
  }
}