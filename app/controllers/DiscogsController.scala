package controllers

import com.google.inject.Inject
import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.discogs4s.entities.Username
import io.bartholomews.fsclient.entities.oauth.ClientCredentials
import javax.inject._
import org.http4s.client.oauth1.Consumer
import play.api.mvc._
import views.common.Tab

import scala.concurrent.ExecutionContext

/**
  *
  */
@Singleton
class DiscogsController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractControllerIO(cc) {

  val consumer: ClientCredentials = ClientCredentials(
    // TODO: ${DISCOGS_CONSUMER_KEY}, ${DISCOGS_CONSUMER_SECRET} (from config)
    Consumer(
      key = "",
      secret = ""
    )
  )

  val discogsClient = new DiscogsClient(userAgent, consumer)

  def hello(): Action[AnyContent] = ActionIO.async {
    discogsClient
      .users.getSimpleUserProfile(Username("_.bartholomews"))
      .map(_.toResult(Tab.Discogs)(user => Ok(views.html.discogs.hello(user.username.value))))
  }

  //  def auth: Action[AnyContent] = Action.async {
  //    discogsClient.auth.getRequestToken
  //      .map(withResponseEntity(requestToken => Redirect(Call("GET", requestToken.callback.renderString))))
  //      .unsafeToFuture()
  //  }

  //  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  //  private def hello(implicit signer: SignerV2): IO[Result] = spotifyClient.users.me map {
  //    fsResponseToPlay(me => Ok(views.html.callback(me.id.value))
  //      .withCookies(Cookie("spotify_access_cookie", (JwtSession() + ("spotify_access_token", signer)).serialize))
  //    )
  //  }

  //  /**
  //    * TODO handleException(e) { _ }
  //    *
  //    * @return
  //    */
  //  def callback: Action[AnyContent] = Action.async {
  //    request => {
  //      val maybeUri = Uri.fromString(s"http://${request.host}${request.uri}")
  //      val getAuthCode = (for {
  //        uri <- EitherT.fromEither[IO](maybeUri.leftMap(parseFailure => BadRequest(parseFailure.details)))
  //        maybeToken <- EitherT.liftF(discogsClient.auth.AuthorizationCode.fromUri(uri))
  //        authorizationCode <- EitherT.fromEither[IO](fsResponseErrorToPlay(maybeToken))
  //      } yield authorizationCode).value
  //
  //      getAuthCode.flatMap(_.fold(
  //        errorResult => IO.pure(errorResult),
  //        authCode => hello(authCode)
  //      )).unsafeToFuture()
  //    }
  //  }
}
