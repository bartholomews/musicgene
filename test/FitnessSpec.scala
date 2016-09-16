import model.constraints._
import model.genetic.{CostBasedFitness, FitnessFunction, Playlist, PlaylistsFactory}
import model.music._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}

/**
  *
  */
class FitnessSpec extends FlatSpec with Matchers with MockitoSugar {

  val emptyConstraints: Set[Constraint] = Set()

  // Mocks
  val p = mock[Playlist]
  val matched = Score(matched = true)
  val unmatched = Score(matched = false)
  val threeOutOfThree = mock[Constraint]
  val noneOutOfThree = mock[Constraint]
  val twoOutOfFour = mock[Constraint]
  // Mocks setup
  when(threeOutOfThree.score(p)).thenReturn(Seq(
    matched, matched, matched))
  when(noneOutOfThree.score(p)).thenReturn(Seq(
    unmatched, unmatched, unmatched))
  when(twoOutOfFour.score(p)).thenReturn(Seq(
    matched, matched, unmatched, unmatched))

  val info1 = Some(Info(Tempo(140), 1, 1.0))
  val info2 = Some(Info(Tempo(140), 2, 2.0))
  val info3 = Some(Info(Tempo(140), 3, 3.0))

  "CostBasedFitness" should "evaluate to 1.0 if all constraints are matched" in {
    CostBasedFitness(Set(threeOutOfThree)).getFitness(p) shouldBe 1.0
  }

  it should "average the final evaluation over the number of constraints" in {
    val f: FitnessFunction = CostBasedFitness(Set(threeOutOfThree, noneOutOfThree, twoOutOfFour))
    f.getFitness(p) shouldBe 0.5
  }

}