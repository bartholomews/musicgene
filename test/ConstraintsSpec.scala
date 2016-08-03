import model.constraints._
import model.geneticScala.{Playlist, StandardFitness}
import model.music._
import org.scalatest.{FlatSpec, Matchers}

/**
  *
  */
class ConstraintsSpec extends FlatSpec with Matchers {

  "A Range Constraint with indexes out of bounds" should "throw exception or do something else" in {
    val c1 = ConstantRange(0, 2, Loudness(10))
// TODO   c1.distance(new Playlist(Vector())) shouldBe 0.0
  }

  "it" should "handle case for a song without the constraint to calculate" in {
    // TODO (return that.value?)
  }

  "ConstantRange with one constraint over two songs " should "calculate distance between them" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3)))
    ))
    val c1 = ConstantRange(0, 1, Loudness(10))
    c1.distance(p1) shouldBe 0.1
  }

  "it" should "sum distances up with one constraints over multiple songs" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.5)))
    ))
    val c1 = ConstantRange(0, 4, Loudness(100))
    c1.distance(p1) shouldBe 0.5
  }

  "it" should "handle positive and negative values" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(- 0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.4))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.2)))
    ))
    val c1 = ConstantRange(0, 4, Loudness(10))
    c1.distance(p1) shouldBe 1.0
  }

  "it" should "ignore songs not in index range" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(- 0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.4))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.2)))
    ))
    val c1 = ConstantRange(0, 1, Loudness(10))
    c1.distance(p1) shouldBe 0.3
  }

  "it" should "be able to produce a negative distance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3)))
    ))
    val c1 = ConstantRange(0, 1, Loudness(10))
    c1.distance(p1) shouldBe 0.1
  }

  //===========================================================================================

  "IncreasingRange" should "sum increasing distances up" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3)))
    ))
    val c1 = IncreasingRange(0, 1, Loudness(10))
    c1.distance(p1) shouldBe 0.1
  }

  "it" should "add penalty value for a non-increasing range, ignoring index over max length" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      // distance = 0.1
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3))),
      // distance = 0.2
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.5))),
      // distance = 0.0 + 10
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.5)))
    ))
    val c1 = IncreasingRange(0, 10, Loudness(10))
    c1.distance(p1) shouldBe 10.3
  }

  "IncreasingRange" should "work with negative values" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4))),
      // distance = 0.2
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.2))),
      // distance = 1.2
      new Song("song1", "_", Set(Title("Title1"), Loudness(1.0))),
      // distance = 0.2
      new Song("song2", "_", Set(Title("Title2"), Loudness(1.2)))
    ))
    val c1 = IncreasingRange(0, 10, Loudness(10))
    c1.distance(p1) shouldBe 1.6
  }

  "it" should "assign an appropriate penalty value" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4))),
      // distance = 10.1
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.5))),
      // distance = 1.5
      new Song("song1", "_", Set(Title("Title1"), Loudness(1.0))),
      // distance = 10.2
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.8)))
    ))
    val c1 = IncreasingRange(0, p1.size, Loudness(10))
    c1.distance(p1) shouldBe 21.8
  }

  //====================================================================================================================

  "IncludeSmaller" should "have 0 cost for values smaller than that" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4)))
    ))
    val c1 = IncludeSmaller(0, Loudness(10), penalty = 100)
    c1.distance(p1) shouldBe 0.0
  }

  "it" should "have 'penalty + distance' cost for values larger than that " in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.4)))
    ))
    val c1 = IncludeSmaller(0, Loudness(0.2), penalty = 100)
    c1.distance(p1) shouldBe 100.2
  }

  "IncludeEquals" should "have 0 cost for values within tolerance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4)))
    ))
    val c1 = IncludeEquals(0, Loudness(0.1), tolerance = 0.5, penalty = 100)
    c1.distance(p1) shouldBe 0.0
  }

  "it" should "have 'penalty + distance' cost for values smaller than tolerance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4)))
    ))
    val c1 = IncludeEquals(0, Loudness(0.2), tolerance = 0.5, penalty = 100)
    c1.distance(p1) shouldBe 100.6
  }

  "it" should "have 'penalty + distance' cost for values larger than tolerance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.4)))
    ))
    val c1 = IncludeEquals(0, Loudness(0.1), tolerance = 0.2, penalty = 100)
    c1.distance(p1) shouldBe 100.3
  }


  /*

  "it" should "" in {

  }

  "it" should "" in {

  }

  "it" should "" in {

  }

  "it" should "" in {

  }

  "it" should "" in {

  }

  */

}












  // =======================================================================================

  //"A UnaryRange constraint" should "work" in {

    //val s1 = new Song("song1", Set(Title("Title"), Loudness(0.2), Mood(Happy)))

    // val c: Constraint = UnaryRange(1, 1.0, 2.0)
  //  val constraints = Set(UnaryRange)
   // val ff = StandardFitness(constraints)
 //   val p: Playlist = new Playlist(Seq(s1), new StandardFitness())
  //  s1.find(Loudness(0.2)) shouldBe Some(Loudness(0.2))
  //  UnaryRange[Loudness](1, 1.0, 2.0)

  /*
  val s1 = new Song("song1", Set(Title("Title"), Year(1999), Mood(Happy)))
  val s2 = new Song("song2", Set(Title("S2"), Year(2000), Mood(Happy)))
  val s3 = new Song("song3", Set(Title("S3"), Year(2000), Mood(Sad)))
  val p1 = new Playlist(Seq(s1, s2, s3))
  val p2 = new Playlist(Seq(s2, s3, s1))
  val p3 = new Playlist(Seq(s3, s1, s2))
  */

  /*
  "An UnaryInclude constraint" should "return false for non-matching attribute" in {
    val para = new ParameterConstraint(p1)
    para.UnaryInclude(1, Year(1999)) shouldBe false
    para.UnaryInclude(1, Year(1999)) shouldBe false
    para.UnaryInclude(1, Year(2000)) shouldBe true
    para.UnaryInclude(1, Title("Wrong_Title")) shouldBe false
    para.UnaryInclude(1, Title("Title")) shouldBe false
    para.UnaryInclude(1, Title("S2")) shouldBe true
    para.UnaryInclude(1, Genre(Rock)) shouldBe false
    para.UnaryInclude(0, Mood(Sad)) shouldBe false
    para.UnaryInclude(0, Mood(Happy)) shouldBe true
    para.UnaryInclude(2, Mood(Sad)) shouldBe true
    para.UnaryInclude(2, Mood(Happy)) shouldBe false
    para.UnaryInclude(3, Mood(Happy)) shouldBe false
  }
  */

  /*
  it should "do" in {
    val theArray = List(
      "6yJJemhHrEX4FZk48D5sAc",
      "6AedV7UWNkUWyOgggA6old",
      "5ULcHWDMA3GKd9Ib4pUSh6",
      "7EipTiVLdacMPjyHs7YF4H",
      "2oerq776NlrwIqlZnLiyab",
      "50SqUUqiHAQZjYoOms3FWY",
      "7JbFBN8rDVzGGxPOuvLEmR",
      "4SQzIk8jUHYK00sJPoythF",
      "29MpHvuhoQw30kBgsPGxsY",
      "4rgHVLNHpDo7p5rii2UMiL",
      "7jCDWdGx2YgEQrE5onvMRl",
      "3f5d8ypmJYDe9rGR8pMcfb",
      "1HIfbJwQRSsmaHyLuxjiu7",
      "2YPa27G9A8Kkv7MFZUMTyB",
      "6NOHmRcPMIk7XQBYXavu1a")
    assert(theArray.forall(s => s.length == 22))
  }
*/