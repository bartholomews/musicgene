import model.constraints._
import model.geneticScala.{Playlist, PlaylistsFactory, StandardFitness}
import model.music._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}

/**
  *
  */
class FitnessSpec extends FlatSpec with Matchers {

  val emptyConstraints: Set[Constraint] = Set()

  val firstTitle3: Constraint = Include(0, Artist("Title_3"))
  val allArtist1: Constraint = IncludeAll(Artist("Artist_1"))
  val allFrom2000: Constraint = IncludeAll(Year(2000))

  val testConstraints: Set[Constraint] = Set(
    firstTitle3,
    allArtist1,
    allFrom2000
  )

  val s1: Song = new Song("s1", Set(Artist("Artist_1"), Title("Title_1"), Year(1999)))
  val s2: Song = new Song("s2", Set(Artist("Artist_1"), Title("Title_2"), Year(2000)))
  val s3: Song = new Song("s3", Set(Artist("Artist_2"), Title("Title_3"), Year(2000)))
  val s4: Song = new Song("s4", Set(Artist("Artist_2"), Title("Title_4"), Year(2000)))
  val s5: Song = new Song("s5", Set(Artist("Artist_3"), Title("Title_5"), Year(2001)))
  val s6: Song = new Song("s6", Set(Artist("Artist_3"), Title("Title_1"), Year(2000)))

  "Test constraints" should "" in {
    val set1 = Set(Artist("A1"), Title("T1"))
    val set2 = Set(Artist("A1"), Title("T2"))
    set1.contains(Title("T2")) shouldBe false
  }

  "A Playlist with no matching constraint" should "have zero fitness" in {
    /*
      track 0 = "Title_3" FALSE
      allArtist1 = FALSE
      allFrom2001 = FALSE
     */
    val p1: Playlist = new Playlist(Vector(s1, s2, s3), new StandardFitness(testConstraints))
      p1.fitness shouldBe 0.0
    }

  /*
    track 0 = "Title_3" FALSE
    allArtist1 = FALSE
    allFrom2000 = TRUE
   */
  "A Playlist with 1/3 matching constraint" should "have fitness 0.33" in {
    val p2 = new Playlist(Vector(s2, s3, s4), new StandardFitness(testConstraints))
    p2.fitness shouldBe 0.33.toFloat
  }

  "A Playlist with 1/4 matching constraints" should "have 0.25 fitness" in {
    /*
      allYear2000 = FALSE
      track 5 = "Artist_1" FALSE (no track 5, shouldn't complain with out of bounds?)
      track 2 = "Title_5" TRUE
      track 2 = "Wrong_Title" FALSE
     */
    val p3 = new Playlist(Vector(s3, s4, s5),
      new StandardFitness(Set(
        IncludeAll(Year(2000)),
        Include(5, Artist("Artist_1")),
        Include(2, Title("Title_5")),
        Include(2, Title("Wrong_Title"))
      ))
    )
    p3.fitness shouldBe 0.25
  }

}

// ===========================================================================

/* OLD WAY OF FitnessCal WAS MORE FUNCTIONAL?
"A Playlist with one matching constraint" should "have fitness 1.0" in {
  FitnessCalc.getFitness(Set(Include(1, Year(2000))), c1) shouldBe 1.0
}
*/
