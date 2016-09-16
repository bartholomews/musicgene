import model.constraints._
import model.genetic.{CostBasedFitness, Playlist}
import model.music._
import org.scalatest.{FlatSpec, Matchers}

/**
  *
  */
class ConstraintsSpec extends FlatSpec with Matchers {
  val p1 = new Playlist(Vector(
    Song("id1", Set(Title("Title_1"), Energy(1.0), Tempo(140))),
    Song("id2", Set(Title("Title_2"), Energy(1.0), Tempo(140))),
    Song("id3", Set(Title("Title_3"), Energy(0.5), Tempo(140))),
    Song("id4", Set(Title("Title_4"), Energy(0.2), Tempo(140))),
    Song("id5", Set(Title("Title_5"), Tempo(140)))
  ), CostBasedFitness(Set()))

  //====================================================================================================================

  "An IncludeAny constraint" should "return one score matched if a playlist include that Attribute" in {
    val score = IncludeAny(Title("Title_1")).score(p1)
    score.size shouldBe 1
    score.head.matched shouldBe true
  }

  it should "return false if a playlist not include that Attribute" in {
    IncludeAny(Danceability(1.0)).score(p1).head.matched shouldBe false
  }

  it should "return false if a playlist include that Attribute with different value" in {
    IncludeAny(Title("Title_0")).score(p1).head.matched shouldBe false
  }

  //====================================================================================================================

  "An IncludeAll constraint" should "return one score matched if all songs include that Attribute" in {
    IncludeAll(Tempo(140)).score(p1).head.matched shouldBe true
  }

  it should "return false if no songs include that Attribute with that value" in {
    IncludeAll(Danceability(1.0)).score(p1).head.matched shouldBe false
  }

  it should "return false if only some songs include that Attribute with that value" in {
    IncludeAll(Energy(1.0)).score(p1).head.matched shouldBe false
  }

  //====================================================================================================================

  "An ExcludeAny constraint" should "return one score matched if one track does not include that Attribute" in {
    ExcludeAny(Energy(1.0)).score(p1).head.matched shouldBe true
  }

  it should "return true if all tracks do not include that Attribute with same value" in {
    ExcludeAny(Danceability(1.0)).score(p1).head.matched shouldBe true
  }

  it should "return false if all tracks include that Attribute with same value" in {
    ExcludeAny(Tempo(140)).score(p1).head.matched shouldBe false
  }

  it should "return true if all tracks include that Attribute with different value" in {
    ExcludeAny(Title("Some other title")).score(p1).head.matched shouldBe true
  }

  //====================================================================================================================

  "An ExcludeAll constraint" should "return one score matched if all tracks do not include that Attribute" in {
    ExcludeAll(Danceability(1.0)).score(p1).head.matched shouldBe true
  }

  it should "return true if all tracks include that Attribute with different value" in {
    ExcludeAll(Title("Some other title")).score(p1).head.matched shouldBe true
  }

  it should "return false if all tracks include that Attribute with same value" in {
    ExcludeAll(Tempo(140)).score(p1).head.matched shouldBe false

  }

  it should "return false if some tracks include that Attribute with same value" in {
    ExcludeAll(Energy(1.0)).score(p1).head.matched shouldBe false
  }

  //====================================================================================================================

  "An IndexedConstraint" should "throw an IndexOutOfBoundsException if indexes are out of range" in {
    intercept[IndexOutOfBoundsException] {
      Include(1, 20, Tempo(140)).score(p1)
    }
  }

  "An Include constraint" should "return all matched Score results if all indexes have that Attribute" in {
    val scores = Include(0, 4, Tempo(140)).score(p1)
    val (matched, unmatched) = scores.partition(p => p.matched)
    matched.size shouldBe 5
    unmatched.size shouldBe 0
  }

  it should "return all unmatched Score results if no indexes have that Attribute" in {
    val scores = Include(0, 4, Danceability(1.0)).score(p1)
    val (matched, unmatched) = scores.partition(p => p.matched)
    matched.size shouldBe 0
    unmatched.size shouldBe 5
  }

  it should "return 'n' matched Score results if 'n' indexes have that Attribute" in {
    val scores = Include(0, 3, Energy(1.0)).score(p1)
    val (matched, unmatched) = scores.partition(p => p.matched)
    matched.size shouldBe 2
    matched.flatMap(s => s.info).map(i => i.index).toSet shouldBe Set(0, 1)
    unmatched.size shouldBe 2
    unmatched.flatMap(s => s.info).map(i => i.index).toSet shouldBe Set(2, 3)
  }

  "An Exclude constraint" should "return all matched Score results if no indexes have that Attribute" in {
    val scores = Exclude(0, 4, Danceability(1.0)).score(p1)
    val (matched, unmatched) = scores.partition(p => p.matched)
    matched.size shouldBe 5
    unmatched.size shouldBe 0
  }

  it should "return all unmatched Score results if all indexes have that Attribute" in {
    val scores = Exclude(0, 4, Tempo(140)).score(p1)
    val (matched, unmatched) = scores.partition(p => p.matched)
    matched.size shouldBe 0
    unmatched.size shouldBe 5
  }

  it should "return 'n' matched Score results if 'n' indexes don't have that Attribute" in {
    val scores = Exclude(0, 3, Energy(1.0)).score(p1)
    val (matched, unmatched) = scores.partition(p => p.matched)
    matched.size shouldBe 2
    matched.flatMap(s => s.info).map(i => i.index).toSet shouldBe Set(2, 3)
    unmatched.size shouldBe 2
    unmatched.flatMap(s => s.info).map(i => i.index).toSet shouldBe Set(0, 1)
  }

  //====================================================================================================================

  "AdjacentInclude" should " " in {
    // TODO
  }

  "AdjacentExclude" should "" in {
    // TODO
  }

  //====================================================================================================================

  "IncludeSmaller" should " " in {
    // TODO
  }

  "IncludeLarger" should "" in {
    // TODO
  }

  "IncludeEquals" should "" in {
    // TODO
  }

  //====================================================================================================================

  "ConstantTransition" should " " in {
    // TODO
  }

  "IncreasingTransition" should "" in {
    // TODO
  }

  "DecreasingTransition" should "" in {
    // TODO
  }

  //====================================================================================================================

  "InRange" should " " in {
    // TODO
  }

  "IncludeLarger" should "" in {
    // TODO
  }


}
/*
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



*/