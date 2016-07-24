package model.geneticScala

import model.constraints.{Constraint, Include, UnaryEqualAny}
import model.music._

/**
  *
  */
object IndividualTest extends App {

  val db = new MusicCollection(Cache.extractSongs)

  println(db.prettyPrintTitleArtist())
//  println("====" * 20)

  val firstAphex = Include(1, Artist("Aphex Twin"))

  val constraints: Set[Constraint] = Set (
    Include(0, Artist("Wu-Tang Clan")),
    Include(1, Title("Liquid Swords")),
    Include(2, Artist("The Wailers")),
    Include(3, Title("I Shot The Sheriff")),
    UnaryEqualAny(Year(2001))
  //  UnaryEqualAny(Year(1960))
  )

  val pop = PopFactory.generatePopulation(db, new StandardFitness(constraints))

  pop.prettyPrint()

  println()

  println("FITTEST: " + pop.getFittest.prettyPrint())
  println("FITNESS: ")
  pop.playlists.foreach(p => println(pop.playlists.indexOf(p) + ": " + pop.getFitness(p)))

}
