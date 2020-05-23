import cats.Applicative
import io.bartholomews.fsclient.config.UserAgent
import io.bartholomews.fsclient.entities.{ErrorBody, ErrorBodyJson, ErrorBodyString}
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import org.http4s.Uri
import play.api.mvc.Results._
import play.api.mvc._
import views.common.Tab

package object controllers {
  def requestHost(request: Request[AnyContent]): String = {
    val scheme = if (request.secure) "https" else "http"
    s"$scheme://${request.host.stripPrefix("/").stripSuffix("/")}"
  }

  val userAgent: UserAgent = UserAgent(
    appName = "musicgene",
    appVersion = Some("0.0.1-SNAPSHOT"),
    appUrl = Some("com.github.bartholomews")
  )

  def redirect(uri: Uri): Result = Redirect(Call("GET", uri.renderString))

  def badRequest(message: String, tab: Tab): Result = BadRequest(views.html.common.error(message, tab))

  def errorToResult(tab: Tab)(error: ErrorBody): Result = error match {
    case ErrorBodyJson(value) => badRequest(value.spaces2, tab)
    // TODO: https://github.com/jilen/play-circe
    case ErrorBodyString(value) => badRequest(value, tab)
  }

  implicit class HttpResponseImplicits[A](httpResponse: HttpResponse[A]) {
    def toResult(tab: Tab)(responseToResult: A => Result): Result =
      httpResponse.foldBody(errorToResult(tab), responseToResult)

    def toResultF[F[_]: Applicative](
      tab: Tab
    )(responseToResult: A => F[Result])(implicit f: Applicative[F]): F[Result] =
      httpResponse.foldBody(err => f.pure(errorToResult(tab)(err)), responseToResult)
  }

}
