import org.openqa.selenium.{JavascriptExecutor, WebDriver}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest._
import org.scalatest.concurrent.Eventually
import selenium._

/**
  * @see http://www.scalatest.org/user_guide/using_selenium
  * @see http://www.artima.com/docs-scalatest-2.0.M5/org/scalatest/selenium/WebBrowser.html
  */
class WebAppSpec extends FlatSpec with ShouldMatchers with WebBrowser with Eventually {

  implicit val webDriver: WebDriver = new HtmlUnitDriver(true)
  // val host = "https://musicgene.herokuapp.com"
  val host = "http://localhost:9000"

  "The web app home page" should "have the correct title" in {
    go to host
    pageTitle should be ("musicgene")
  }

  "Login button" should "send the user to spotify accounts after being clicked" in {
    go to host
    click on "login-button"
    val domain = executeScript("return document.domain;")
    domain should be ("accounts.spotify.com")
  }

  "Try the App button" should "redirect to MIR title page after being clicked" in {
    go to host
    click on "try-button"
    eventually { pageTitle should be ("MIR") }
  }

}
