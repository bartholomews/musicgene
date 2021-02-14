package io.bartholomews.musicgene.model.spotify

import cats.Monad
import io.bartholomews.fsclient.core.http.SttpResponses.SttpResponse
import io.bartholomews.fsclient.core.oauth.SignerV2
import io.bartholomews.fsclient.core.oauth.v2.OAuthV2.RedirectUri
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import io.bartholomews.musicgene.controllers.routes
import io.bartholomews.spotify4s.core.SpotifyClient
import io.bartholomews.spotify4s.core.api.AuthApi.SpotifyUserAuthorizationRequest
import io.bartholomews.spotify4s.core.entities.{Page, PrivateUser, SimplePlaylist, SpotifyScope}
import play.api.libs.json.JsError
import play.api.mvc.RequestHeader
import sttp.client3.{ResponseException, SttpBackend, UriContext}
import sttp.model.Uri

class SpotifyService[F[_]: Monad](backend: SttpBackend[F, Any]) {

  import io.bartholomews.spotify4s.playJson.codecs._

  val client: SpotifyClient[F] = SpotifyClient.unsafeFromConfig[F](backend)

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

  def me(implicit signer: SignerV2): F[SttpResponse[JsError, PrivateUser]] = client.users.me

  def authorizeUrl(session: SpotifySessionUser)(implicit request: RequestHeader): Uri =
    client.auth.authorizeUrl(
      userAuthorizationRequest(session),
      showDialog = true
    )

  def getUserAndPlaylists(
    implicit signer: SignerV2
  ): F[Either[ResponseException[String, JsError], (PrivateUser, Page[SimplePlaylist])]] = {
    val getUser = client.users.me.map(_.body)
    val getUserPlaylists = client.users.getPlaylists(limit = 50).map(_.body)

    (getUser, getUserPlaylists)
    //      .parMapN({
      .mapN({
        case (aaa, bbb) =>
          for {
            privateUser <- aaa
            playlists <- bbb
          } yield Tuple2(privateUser, playlists)
      })
  }
}
