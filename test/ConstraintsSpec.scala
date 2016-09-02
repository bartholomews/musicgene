import model.constraints._
import model.genetic.{CostBasedFitness, Playlist}
import model.music._
import org.scalatest.{FlatSpec, Matchers}

/**
  *
  */

/*
class ConstraintsSpec extends FlatSpec with Matchers {

  "A Range Constraint with indexes out of bounds" should "throw exception or do something else" in {
    val c1 = ConstantRange(0, 2, Loudness(10))
// TODO   c1.distance(new Playlist(Vector())) shouldBe 0.0
  }

  "it" should "handle case for a song without the constraint to calculate" in {
    // TODO (return that.value?)
  }

  "ConstantRange with one constraint over two songs " should "calculate distance between them" in {
    val c1 = ConstantRange(0, 1, Loudness(10))
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3)))
    ))
    c1.score(p1) shouldBe 0.1
  }

  "it" should "sum distances up with one constraints over multiple songs" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.5)))
    ))
    val c1 = ConstantRange(0, 4, Loudness(100))
    c1.score(p1) shouldBe 0.5
  }

  "it" should "handle positive and negative values" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(- 0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.4))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.2)))
    ))
    val c1 = ConstantRange(0, 4, Loudness(10))
    c1.score(p1) shouldBe 1.0
  }

  "it" should "ignore songs not in index range" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(- 0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.1))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.4))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(- 0.2)))
    ))
    val c1 = ConstantRange(0, 1, Loudness(10))
    c1.score(p1) shouldBe 0.3
  }

  "it" should "be able to produce a negative distance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3)))
    ))
    val c1 = ConstantRange(0, 1, Loudness(10))
    c1.score(p1) shouldBe 0.1
  }

  //===========================================================================================

  "IncreasingRange" should "sum increasing distances up" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3)))
    ))
    val c1 = IncreasingRange(0, 1, Loudness(10))
    c1.score(p1) shouldBe 0.1
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
    c1.score(p1) shouldBe 10.3
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
    c1.score(p1) shouldBe 1.6
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
    c1.score(p1) shouldBe 21.8
  }

  //===========================================================================================

  "DecreasingRange" should "sum decreasing distances up" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.3))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.2)))
    ))
    val c1 = DecreasingRange(0, 1, Loudness(10))
    c1.score(p1) shouldBe 0.1
  }

  "it" should "add penalty value for a non-decreasing range, ignoring index over max length" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      // distance = 10.1
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.3))),
      // distance = 10.2
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.5))),
      // distance = 0.1
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.4)))
    ))
    val c1 = DecreasingRange(0, 10, Loudness(10))
    c1.score(p1) shouldBe 20.4
  }

  "DecreasingRange" should "work with negative values" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4))),
      // distance = 0.2
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.6))),
      // distance = 11.6
      new Song("song1", "_", Set(Title("Title1"), Loudness(1.0))),
      // distance = 1.0
      new Song("song2", "_", Set(Title("Title2"), Loudness(0.0)))
    ))
    val c1 = DecreasingRange(0, 10, Loudness(10))
    c1.score(p1) shouldBe 12.8
  }

  "DecreasingRange" should "assign an appropriate penalty value" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4))),
      // distance = 0.1
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.5))),
      // distance = 11.5
      new Song("song1", "_", Set(Title("Title1"), Loudness(1.0))),
      // distance = 10.8
      new Song("song2", "_", Set(Title("Title2"), Loudness(1.8)))
    ))
    val c1 = DecreasingRange(0, p1.size, Loudness(10))
    c1.score(p1) shouldBe 22.4
  }

  //====================================================================================================================

  "IncludeSmaller" should "have 0 cost for values smaller than that" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4)))
    ))
    val c1 = IncludeSmaller(0, 0, Loudness(10), penalty = 100)
    c1.score(p1) shouldBe 0.0
  }

  "it" should "have 'penalty + distance' cost for values larger than that " in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.4)))
    ))
    val c1 = IncludeSmaller(0, 0, Loudness(0.2), penalty = 100)
    c1.score(p1) shouldBe 100.2
  }

  "IncludeSmaller" should "have same value as its equivalent series of single indexes constraints" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(1.5))),
      new Song("song3", "_", Set(Title("Title3"), Loudness(3.2))),
      new Song("song4", "_", Set(Title("Title4"), Loudness(-0.4)))
    ))
    val d1 = IncludeSmaller(0, 3, Loudness(0.5), penalty = 100).score(p1)
    val d2: Double = List(
      IncludeSmaller(0, 0, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance),
      IncludeSmaller(1, 1, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance),
      IncludeSmaller(2, 2, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance),
      IncludeSmaller(3, 3, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance)
    ).flatten.sum
    d1 should equal(d2)
  }

  //====================================================================================================================

  "IncludeLarger" should "have 0 cost for values larger than that" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4)))
    ))
    val c1 = IncludeLarger(0, 0, Loudness(-0.5), penalty = 100)
    c1.score(p1) shouldBe 0.0
  }

  "it" should "have 'penalty + distance' cost for values smaller than that " in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.4)))
    ))
    val c1 = IncludeLarger(0, 0, Loudness(0.8), penalty = 100)
    c1.score(p1) shouldBe 100.4
  }

  "IncludeLarger" should "have same value as its equivalent series of single indexes constraints" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(1.5))),
      new Song("song3", "_", Set(Title("Title3"), Loudness(3.2))),
      new Song("song4", "_", Set(Title("Title4"), Loudness(-0.4)))
    ))
    val d1 = IncludeLarger(0, 3, Loudness(0.5), penalty = 100).score(p1)
    val d2: Double = List(
      IncludeLarger(0, 0, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance),
      IncludeLarger(1, 1, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance),
      IncludeLarger(2, 2, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance),
      IncludeLarger(3, 3, Loudness(0.5), penalty = 100).score(p1).map(s => s.distance)
    ).flatten.sum
    d1 should equal(d2)
  }

  //====================================================================================================================

  "IncludeEquals" should "have 0 cost for values within tolerance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4)))
    ))
    val c1 = IncludeEquals(0, 0, Loudness(0.1), tolerance = 0.5, penalty = 100)
    c1.score(p1) shouldBe 0.0
  }

  "it" should "have 'penalty + distance' cost for values smaller than tolerance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(-0.4)))
    ))
    val c1 = IncludeEquals(0, 0, Loudness(0.2), tolerance = 0.5, penalty = 100)
    c1.score(p1) shouldBe 100.6
  }

  "it" should "have 'penalty + distance' cost for values larger than tolerance" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.4)))
    ))
    val c1 = IncludeEquals(0, 0, Loudness(0.1), tolerance = 0.2, penalty = 100)
    c1.score(p1) shouldBe 100.3
  }

  "IncludeEquals" should "have same value as its equivalent series of single indexes constraints" in {
    val p1 = new Playlist(Vector(
      new Song("song1", "_", Set(Title("Title1"), Loudness(0.2))),
      new Song("song2", "_", Set(Title("Title2"), Loudness(1.5))),
      new Song("song3", "_", Set(Title("Title3"), Loudness(3.2))),
      new Song("song4", "_", Set(Title("Title4"), Loudness(-0.4)))
    ))
    val d1 = IncludeEquals(0, 3, Loudness(0.5), tolerance = 0.5, penalty = 100).score(p1)
    val d2: Double = List(
      IncludeEquals(0, 0, Loudness(0.5), tolerance = 0.5, penalty = 100).score(p1).map(s => s.distance),
      IncludeEquals(1, 1, Loudness(0.5), tolerance = 0.5, penalty = 100).score(p1).map(s => s.distance),
      IncludeEquals(2, 2, Loudness(0.5), tolerance = 0.5, penalty = 100).score(p1).map(s => s.distance),
      IncludeEquals(3, 3, Loudness(0.5), tolerance = 0.5, penalty = 100).score(p1).map(s => s.distance)
    ).flatten.sum
    d1 should equal(d2)
  }

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



*/