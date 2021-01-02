package io.bartholomews.musicgene.controllers

import cats.data.EitherT
import cats.effect.IO
import com.google.inject.Inject
import io.bartholomews.discogs4s.DiscogsClient
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.RedirectUri
import io.bartholomews.fsclient.core.oauth.{AccessTokenCredentials, SignerV1, TokenCredentials}
import io.bartholomews.musicgene.controllers.http.DiscogsCookies
import javax.inject._
import play.api.mvc._
import sttp.client.UriContext
import sttp.model.Uri

import scala.concurrent.ExecutionContext

/**
 *
 */
@Singleton
class DiscogsController @Inject() (cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractControllerIO(cc) {

  import cats.implicits._
  import io.bartholomews.musicgene.controllers.http.DiscogsHttpResults._
  import io.bartholomews.musicgene.controllers.http.codecs.FsClientCodecs._

  // TODO: load from config
  private val discogsCallback = RedirectUri(uri"http://localhost:9000/discogs/callback")

  val discogsClient: DiscogsClient[IO, SignerV1] = DiscogsClient.clientCredentialsFromConfig

  private def hello(signer: SignerV1)(implicit request: Request[AnyContent]): IO[Result] =
    discogsClient.users.me(signer).map(_.toResult(me => Ok(views.html.discogs.hello(me))))

  def hello(): Action[AnyContent] = ActionIO.async { implicit request =>
    withToken(hello)
  }

  def callback: Action[AnyContent] = ActionIO.async { implicit request =>
    DiscogsCookies
      .extract[AccessTokenCredentials](request)(accessTokenV1Format)
      .map(hello)
      .getOrElse {


        val query = request.rawQueryString
        val maybeUri = Uri.parse(s"${routes.DiscogsController.callback().absoluteURL()}?$query")

        def extractSessionRequestToken: Either[Result, TemporaryCredentials] =
          DiscogsCookies
            .extract[TemporaryCredentials](request)
            .toRight(badRequest("There was a problem retrieving the request token"))

        val watt: IO[Either[Result, AccessTokenCredentials]] = (for {
          requestToken <- EitherT.fromEither[IO](extractSessionRequestToken)
          callbackUri <- EitherT.fromEither[IO](
            maybeUri.leftMap(parseFailure => badRequest(parseFailure))
          )
          maybeAccessToken <- EitherT.liftF(discogsClient.auth.fromUri(callbackUri, requestToken))
          accessTokenCredentials <- EitherT.fromEither[IO](maybeAccessToken.body.leftMap(errorToResult))
        } yield accessTokenCredentials).value

        watt.flatMap(
            _.fold(
              errorResult => IO.pure(errorResult),
              signer => hello(signer).map(_.withCookies(DiscogsCookies.accessCookie(signer)))
            )
          )
      }
  }

  def logout(): Action[AnyContent] = ActionIO.async { implicit request =>
    IO.pure {
      DiscogsCookies.extract[AccessTokenCredentials](request) match {
        case None => BadRequest("Need to be token-authenticated to logout!")
        case Some(accessToken: TokenCredentials) =>
          redirect(DiscogsAuthEndpoint.revokeUri(accessToken))
            .discardingCookies(DiscogsCookies.discardCookies)
      }
    }
  }

  /**
   * Redirect a user to authenticate with Discogs and grant permissions to the application
   *
   * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
   */
  private def authenticate(implicit request: Request[AnyContent]) =
    discogsClient.auth
      .getRequestToken(discogsClient.temporaryCredentialsRequest(discogsCallback))
      .map(
        _.body
          .fold[Result](
            errorToResult,
            (requestToken: TemporaryCredentials) =>
              redirect(requestToken.resourceOwnerAuthorizationRequest)
                .withCookies(DiscogsCookies.accessCookie(requestToken))
          )
      )

  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  def withToken[A](f: SignerV1 => IO[Result])(implicit request: Request[AnyContent]): IO[Result] =
    DiscogsCookies.extract[AccessTokenCredentials](request) match {
      case None              => authenticate(request)
      case Some(accessToken) => f(accessToken)
    }
}
