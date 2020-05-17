package controllers

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.google.inject.Inject
import controllers.http.DiscogsCookies
import controllers.http.JsonProtocol._
import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.discogs4s.entities.RequestToken
import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.SignerType
import io.bartholomews.fsclient.entities.oauth.{AccessTokenCredentials, SignerV1}
import javax.inject._
import org.http4s.Uri
import play.api.mvc._
import views.common.Tab

import scala.concurrent.ExecutionContext

/**
 *
 */
@Singleton
class DiscogsController @Inject() (cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractControllerIO(cc) {

  // TODO: load from config
  private val discogsCallback = Uri.unsafeFromString("http://localhost:9000/discogs/callback")

  val discogsClient: DiscogsClient =
    DiscogsClient.unsafeFromConfig(SignerType.BasicSignature)

//  val discogsClient = DiscogsClient.unsafeFromConfig(
//    SignerType.TokenSignature
//  )  (userAgent, consumer)

  private def hello(signer: SignerV1): IO[Result] =
    discogsClient.auth.me(signer).map(_.toResult(Tab.Spotify)(me => Ok(views.html.discogs.hello(me))))

  def hello(): Action[AnyContent] = ActionIO.async {
    withToken(hello)
  }

  def callback: Action[AnyContent] = ActionIO.async { request =>
    val maybeUri = Uri.fromString(s"${requestHost(request)}/${request.uri.stripPrefix("/")}")

    def getRequestTokenFromCookies: Either[Result, RequestToken] =
      DiscogsCookies
        .getCookieCredentials[RequestToken](request)
        .toRight(
          badRequest("There was a problem retrieving the request token", Tab.Discogs)
        )

    (for {
      requestToken <- EitherT.fromEither[IO](getRequestTokenFromCookies)
      callbackUri <- EitherT.fromEither[IO](
        maybeUri.leftMap(parseFailure => badRequest(parseFailure.details, Tab.Spotify))
      )
      maybeAccessToken <- EitherT.liftF(discogsClient.auth.fromUri(requestToken, callbackUri))
      accessTokenCredentials <- EitherT.fromEither[IO](maybeAccessToken.entity.leftMap(errorToResult(Tab.Discogs)))
    } yield accessTokenCredentials).value
      .flatMap(
        _.fold(
          errorResult => IO.pure(errorResult),
          signer => hello(signer).map(_.withCookies(DiscogsCookies.serializeCookieCredentials(signer)))
        )
      )
  }

  def logout(): Action[AnyContent] = ActionIO.async {
    DiscogsCookies.getCookieCredentials()
  }

  /**
   * Redirect a user to authenticate with Discogs and grant permissions to the application
   *
   * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
   */
  private def authenticate: IO[Result] =
    discogsClient.auth
      .getRequestToken(discogsClient.temporaryCredentialsRequest(discogsCallback))
      .map(
        _.entity
          .fold(
            errorToResult(Tab.Discogs),
            (requestToken: RequestToken) =>
              Redirect(Call("GET", requestToken.callback.renderString))
                .withCookies(DiscogsCookies.serializeCookieCredentials(requestToken))
          )
      )

  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  def withToken[A](f: SignerV1 => IO[Result]): Request[AnyContent] => IO[Result] = { request =>
    DiscogsCookies.getCookieCredentials[AccessTokenCredentials](request) match {
      case None              => authenticate
      case Some(accessToken) => f(accessToken)
    }
  }

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
