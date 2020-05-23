package controllers

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.google.inject.Inject
import controllers.http.DiscogsSession
import controllers.http.JsonProtocol._
import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint
import io.bartholomews.discogs4s.entities.RequestToken
import io.bartholomews.fsclient.entities.oauth.v1.OAuthV1AuthorizationFramework.SignerType
import io.bartholomews.fsclient.entities.oauth.{AccessTokenCredentials, SignerV1, TokenCredentials}
import javax.inject._
import org.http4s.Uri
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 *
 */
@Singleton
class DiscogsController @Inject() (cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractControllerIO(cc) {

  import controllers.http.DiscogsHttpResults._

  // TODO: load from config
  private val discogsCallback = Uri.unsafeFromString("http://localhost:9000/discogs/callback")

  val discogsClient: DiscogsClient =
    DiscogsClient.unsafeFromConfig(SignerType.BasicSignature)

  private def hello(signer: SignerV1): IO[Result] =
    discogsClient.auth.me(signer).map(_.toResult(me => Ok(views.html.discogs.hello(me))))

  def hello(): Action[AnyContent] = ActionIO.async {
    withToken(hello)
  }

  def callback: Action[AnyContent] = ActionIO.async { implicit request =>
    DiscogsSession
      .getSession[AccessTokenCredentials](request)
      .map(hello)
      .getOrElse {

        val maybeUri = Uri.fromString(s"${requestHost(request)}/${request.uri.stripPrefix("/")}")

        def extractSessionRequestToken: Either[Result, RequestToken] =
          DiscogsSession
            .getSession[RequestToken](request)
            .toRight(
              badRequest("There was a problem retrieving the request token")
            )

        (for {
          requestToken <- EitherT.fromEither[IO](extractSessionRequestToken)
          callbackUri <- EitherT.fromEither[IO](
            maybeUri.leftMap(parseFailure => badRequest(parseFailure.details))
          )
          maybeAccessToken <- EitherT.liftF(discogsClient.auth.fromUri(requestToken, callbackUri))
          accessTokenCredentials <- EitherT.fromEither[IO](maybeAccessToken.entity.leftMap(errorToResult))
        } yield accessTokenCredentials).value
          .flatMap(
            _.fold(
              errorResult => IO.pure(errorResult),
              signer => hello(signer).map(_.addingToSession(DiscogsSession.serializeSession(signer)))
            )
          )
      }
  }

  def logout(): Action[AnyContent] = ActionIO.async { implicit request =>
    IO.pure {
      DiscogsSession.getSession[AccessTokenCredentials](request) match {
        case None => BadRequest("Need to be token-authenticated to logout!")
        case Some(accessToken: TokenCredentials) =>
          redirect(DiscogsAuthEndpoint.revokeUri(accessToken))
            .removingFromSession(DiscogsSession.sessionKey)
      }
    }
  }

  /**
   * Redirect a user to authenticate with Discogs and grant permissions to the application
   *
   * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
   */
  private def authenticate(implicit request: Request[AnyContent]): IO[Result] =
    discogsClient.auth
      .getRequestToken(discogsClient.temporaryCredentialsRequest(discogsCallback))
      .map(
        _.entity
          .fold(
            errorToResult,
            (requestToken: RequestToken) =>
              redirect(requestToken.callback)
                .addingToSession(DiscogsSession.serializeSession(requestToken))
          )
      )

  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  def withToken[A](f: SignerV1 => IO[Result]): Request[AnyContent] => IO[Result] = { request =>
    DiscogsSession.getSession[AccessTokenCredentials](request) match {
      case None              => authenticate(request)
      case Some(accessToken) => f(accessToken)
    }
  }
}
