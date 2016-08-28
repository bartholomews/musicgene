import model.music._
import org.scalatest.{FlatSpec, Matchers}

/**
  *
  */
class SongSpec extends FlatSpec with Matchers {

  "A Song" should "find" in {

    val s1 = SpotifySong("song1", Set(Title("Title"), Loudness(0.2), Mood(Happy)))

  }
}
