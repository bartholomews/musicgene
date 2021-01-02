package io.bartholomews.musicgene.model.spotify

import cats.NonEmptyParallel
import cats.effect.ConcurrentEffect
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.SignerV2
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.RedirectUri
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import io.bartholomews.musicgene.controllers.routes
import io.bartholomews.spotify4s.core.SpotifyClient
import io.bartholomews.spotify4s.core.api.AuthApi.SpotifyUserAuthorizationRequest
import io.bartholomews.spotify4s.core.entities.{Page, PrivateUser, SimplePlaylist, SpotifyScope}
import io.circe
import play.api.mvc.RequestHeader
import sttp.client.{ResponseError, SttpBackend, UriContext}
import sttp.model.Uri

class SpotifyService[F[_]: ConcurrentEffect: NonEmptyParallel]()(implicit backend: SttpBackend[F, Nothing, Nothing]) {

  import io.bartholomews.spotify4s.circe._

  val client: SpotifyClient[F] = SpotifyClient.unsafeFromConfig[F]()

  import cats.implicits._
  import eu.timepit.refined.auto.autoRefineV

  def userAuthorizationRequest(session: SpotifySessionUser)(
    implicit request: RequestHeader
  ): SpotifyUserAuthorizationRequest = SpotifyUserAuthorizationRequest(
    redirectUri = RedirectUri(uri"${routes.SpotifyController.callback(session).absoluteURL()}"),
    state = None, // TODO
    scopes = List(
      // FIXME: Scope based on main/src
      SpotifyScope.PLAYLIST_MODIFY_PRIVATE,
      SpotifyScope.PLAYLIST_MODIFY_PUBLIC,
      SpotifyScope.PLAYLIST_READ_PRIVATE,
      SpotifyScope.PLAYLIST_READ_COLLABORATIVE
    )
  )

  def me(implicit signer: SignerV2): F[SttpResponse[circe.Error, PrivateUser]] = client.users.me

  def authorizeUrl(session: SpotifySessionUser)(implicit request: RequestHeader): Uri =
    client.auth.authorizeUrl(
      userAuthorizationRequest(session),
      showDialog = true
    )

  def getUserAndPlaylists(
    implicit signer: SignerV2
  ): F[Either[ResponseError[circe.Error], (PrivateUser, Page[SimplePlaylist])]] = {
    val getUser = client.users.me.map(_.body)
    val getUserPlaylists = client.users.getPlaylists(limit = 50).map(_.body)

    (getUser, getUserPlaylists)
      .parMapN({
        case (aaa, bbb) =>
          for {
            privateUser <- aaa
            playlists <- bbb
          } yield Tuple2(privateUser, playlists)
      })
  }
}
