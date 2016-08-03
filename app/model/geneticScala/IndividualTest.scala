package model.geneticScala

import model.constraints._
import model.music._

/**
  *
  */
object IndividualTest extends App {


  //===========

  val constraints: Set[ScoreConstraint] = Set(
    IncludeSmaller(0, Tempo(90), 10),
    IncludeSmaller(1, Tempo(90), 10),
    IncludeSmaller(2, Tempo(90), 10),
    IncludeSmaller(3, Tempo(90), 10),
    IncludeSmaller(4, Tempo(90), 10),
    IncludeSmaller(5, Tempo(90), 10),
    IncludeSmaller(6, Tempo(90), 10),
    IncludeSmaller(7, Tempo(90), 10),
    IncreasingRange(0, 10, Tempo(100)),
    DecreasingRange(10, 20, Tempo(100))
  )

  /*

  val constraints2: Set[Constraint] = Set(
    /*
    UnaryEqualAny(Title("Tha")),
    Include(0, Artist("Aphex Twin")),
    Include(1, Artist("Aphex Twin")),
    Include(2, Artist("Aphex Twin")),
    Include(3, Title("Brooklyn Zoo")),
    Include(4, Title("Method Man")),
    */
    // no song with tempo 102.397
    //   UnaryEqualNone(Artist("Aphex Twin"))
    // this BinaryLarger will get 0 of course even if it finds 99% of it.
    // maybe if you translate this kinds of attribute into a set of constraints foreach indices it works better
    // e.g.
    //    UnarySmallerNone(Tempo(120))
    // ==, that's right
    IncludeSmaller(0, Tempo(120)),
    IncludeLarger(1, Tempo(120)),
    IncludeEquals(2, Tempo(120), 10),
    IncludeSmaller(3, Tempo(120)),
    IncludeLarger(4, Tempo(120)),
    IncludeSmaller(5, Tempo(120)),
    IncludeLarger(5, Tempo(120)),
    IncludeSmaller(6, Tempo(120)),
    IncludeLarger(5, Tempo(120)),
    IncludeSmaller(5, Tempo(120)),
    IncludeLarger(5, Tempo(120)),
    //  careful with IndexOutOfBounds
    Include(5, Artist("Aphex Twin")),
    Include(6, Artist("Aphex Twin")),
    Include(7, Artist("Aphex Twin"))
   //   Include(8, Title("A Drifting Up")),
 //   Include(9, Title("#1"))
 //   Include(10, Title("Come As You Are"))
  )

  */

  // with few songs get stuck, whole database length eventually gets a good score
  val p = GA.generatePlaylist(CostBasedFitness(constraints), 50)
  println("GENERATED PLAYLIST: ")
  p.prettyPrint()



  //===========

  val db = new MusicCollection(Cache.extractSongs)

  println(db.prettyPrintTitleArtist())
//  println("====" * 20)

  val firstAphex = Include(1, Artist("Aphex Twin"))

  /*
  val constraints: Set[Constraint] = Set (
    Include(0, Artist("Wu-Tang Clan")),
    Include(1, Title("Liquid Swords")),
    Include(2, Artist("The Wailers")),
    Include(3, Title("I Shot The Sheriff")),
    UnaryEqualAny(Year(2001))
  //  UnaryEqualAny(Year(1960))
  )
  */

  val scoreConstraint: Set[ScoreConstraint] = Set(
    IncreasingRange(0, 1, Loudness(10))
  )

  val pop = PopFactory.generatePopulation(db, CostBasedFitness(scoreConstraint))

  pop.prettyPrint()

  println()

  println("FITTEST: " + pop.getFittest.prettyPrint())
  println("FITNESS: ")
  pop.playlists.foreach(p => println(pop.playlists.indexOf(p) + ": " + pop.getFitness(p)))

}
