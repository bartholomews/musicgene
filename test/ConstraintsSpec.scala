import org.scalatest.{FlatSpec, Matchers}

/**
  *
  */
class ConstraintsSpec extends FlatSpec with Matchers {

  "A Constraint out of bounds" should "throw exception or do something else" in {

  }

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

}
