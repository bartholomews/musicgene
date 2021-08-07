package io.bartholomews.musicgene.controllers

import cats.data.EitherT
import com.google.inject.Inject
import io.bartholomews.discogs4s.{DiscogsClient, DiscogsOAuthClient}
import io.bartholomews.discogs4s.endpoints.DiscogsAuthEndpoint
import io.bartholomews.fsclient.core.oauth.v1.TemporaryCredentials
import io.bartholomews.fsclient.core.oauth.{AccessTokenCredentials, RedirectUri, SignerV1, TokenCredentials}
import io.bartholomews.musicgene.controllers.http.DiscogsCookies
import javax.inject._
import play.api.mvc._
import sttp.client3.{SttpBackend, UriContext}
import sttp.model.Uri

import scala.concurrent.{ExecutionContext, Future}
/**
 *
 */
@Singleton
class DiscogsController @Inject() (cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  import cats.implicits._
  import io.bartholomews.discogs4s.playJson.codecs._
  import io.bartholomews.musicgene.controllers.http.DiscogsHttpResults._

  // TODO: load from config
  private val discogsCallback = RedirectUri(uri"http://localhost:9000/discogs/callback")

  import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend
  implicit val discogsBackend: SttpBackend[Future, Any] = AsyncHttpClientFutureBackend()

  val discogsClient: DiscogsOAuthClient[Future] =
    DiscogsClient.oAuth.unsafeFromConfig(discogsBackend)

  private def hello(signer: SignerV1)(implicit request: Request[AnyContent]): Future[Result] =
    discogsClient.users.me(signer).map(_.toResult(me => Ok(views.html.discogs.hello(me))))

  def hello(): Action[AnyContent] = Action.async { implicit request =>
    withToken(hello)
  }

  def callback: Action[AnyContent] = Action.async { implicit request =>
    DiscogsCookies
      .extract[AccessTokenCredentials](request)
      .map(hello)
      .getOrElse {

        val query = request.rawQueryString
        val maybeUri = Uri.parse(s"${routes.DiscogsController.callback().absoluteURL()}?$query")

        def extractSessionRequestToken: Either[Result, TemporaryCredentials] =
          DiscogsCookies
            .extract[TemporaryCredentials](request)
            .toRight(badRequest("There was a problem retrieving the request token"))

        val watt: Future[Either[Result, AccessTokenCredentials]] = (for {
          requestToken <- EitherT.fromEither[Future](extractSessionRequestToken)
          callbackUri <- EitherT.fromEither[Future](
            maybeUri.leftMap(parseFailure => badRequest(parseFailure))
          )
          maybeAccessToken <- EitherT.liftF(discogsClient.auth.fromUri(callbackUri, requestToken))
          accessTokenCredentials <- EitherT.fromEither[Future](maybeAccessToken.body.leftMap(errorToResult))
        } yield accessTokenCredentials).value

        watt.flatMap(
          _.fold(
            errorResult => Future.successful(errorResult),
            signer => hello(signer).map(_.withCookies(DiscogsCookies.accessCookie(signer)))
          )
        )
      }
  }

  def logout(): Action[AnyContent] = Action.async { implicit request =>
    Future {
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
  def withToken[A](f: SignerV1 => Future[Result])(implicit request: Request[AnyContent]): Future[Result] =
    DiscogsCookies.extract[AccessTokenCredentials](request) match {
      case None              => authenticate(request)
      case Some(accessToken) => f(accessToken)
    }
}
