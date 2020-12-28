package io.bartholomews.musicgene.model.spotify

import cats.NonEmptyParallel
import cats.effect.ConcurrentEffect
import io.bartholomews.fsclient.core.oauth.SignerV2
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import io.bartholomews.musicgene.controllers.routes
import io.bartholomews.spotify4s.SpotifyClient
import io.bartholomews.spotify4s.entities.{Page, PrivateUser, SimplePlaylist, SpotifyScope}
import io.circe
import play.api.mvc.RequestHeader
import sttp.client.{Response, ResponseError, SttpBackend, UriContext}
import sttp.model.Uri

class SpotifyService[F[_]: ConcurrentEffect: NonEmptyParallel]()(implicit backend: SttpBackend[F, Nothing, Nothing]) {

  val client: SpotifyClient[F] = SpotifyClient.unsafeFromConfig[F]()

  import cats.implicits._
  import eu.timepit.refined.auto.autoRefineV

  def me(implicit signer: SignerV2): F[Response[Either[ResponseError[circe.Error], PrivateUser]]] = client.users.me

  def authorizeUrl(session: SpotifySessionUser)(implicit request: RequestHeader): Uri =
    client.auth.authorizeUrl(
      redirectUri = uri"${routes.SpotifyController.callback(session).absoluteURL()}",
//      redirectUri = Uri(
//        scheme = Some(scheme),
//        path = (host / Segment("spotify") / Segment(session.entryName) / Segment("callback")).dropEndsWithSlash
//      ),
      state = None,
      scopes = List(
        // FIXME: Scope based on main/src
        SpotifyScope.PLAYLIST_MODIFY_PRIVATE,
        SpotifyScope.PLAYLIST_MODIFY_PUBLIC,
        SpotifyScope.PLAYLIST_READ_PRIVATE,
        SpotifyScope.PLAYLIST_READ_COLLABORATIVE
      ),
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
