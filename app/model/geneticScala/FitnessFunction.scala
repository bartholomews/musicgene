package model.geneticScala

import model.constraints.Constraint
import model.music.{Duration, Song}

/**
  * SHOULD GIVE SOME POINTS FOR PARTIALLY SUCCESSFUL ONES
  * Each Playlist can be assigned a different kind of FitnessCalc
  */
trait FitnessFunction {
  val constraints: Set[Constraint]
  def getFitness(playlist: Playlist): Float
}

  case class StandardFitness(constraints: Set[Constraint]) extends FitnessFunction {
    /**
      * Fitness function defined as the number of matched constraints in a Playlist,
      * as a decimal percentage rounded to the nearest hundredth.
      *
      * TODO some points for partially successful ones?
      * how to deal with constraint relative to relationship between tracks?
      *
      * @param playlist the Playlist to be tested against the constraints
      * @return
      */
    def getFitness(playlist: Playlist): Float = {
      val f = constraints.count(constraint => constraint.calc(playlist)) / constraints.size.toFloat
      BigDecimal.decimal(f).setScale(2, BigDecimal.RoundingMode.HALF_UP).toFloat
    }
  }

    case class SelectionFitness(constraints: Set[Constraint]) extends FitnessFunction {
      def getFitness(p: Playlist) = 0
      def getFitness(s: Song): Float = {
        //    val f = constraints.count(c => // SONG!)
        0 //TODO
      }
    }
