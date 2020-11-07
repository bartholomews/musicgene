package io.bartholomews.musicgene.model.spotify

import cats.NonEmptyParallel
import cats.effect.{ConcurrentEffect, ContextShift}
import io.bartholomews.fsclient.entities.ErrorBody
import io.bartholomews.fsclient.entities.oauth.SignerV2
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import io.bartholomews.spotify4s.SpotifyClient
import io.bartholomews.spotify4s.entities.{Page, PrivateUser, SimplePlaylist, SpotifyScope}
import org.http4s.Uri
import org.http4s.Uri.Path.Segment

import scala.concurrent.ExecutionContext

class SpotifyService[F[_]: ConcurrentEffect: NonEmptyParallel]()(
  implicit ec: ExecutionContext, cs: ContextShift[F]
) {

  val client: SpotifyClient[F] = SpotifyClient.unsafeFromConfig[F]()

  import cats.implicits._
  import eu.timepit.refined.auto.autoRefineV

  def me(implicit signer: SignerV2): F[HttpResponse[PrivateUser]] = client.users.me

  def authorizeUrl(scheme: Uri.Scheme, host: Uri.Path, session: SpotifySessionUser): Uri =
    client.auth.authorizeUrl(
      redirectUri = Uri(
        scheme = Some(scheme),
        path = (host / Segment("spotify") / Segment(session.entryName) / Segment("callback")).dropEndsWithSlash
      ),
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

  def getUserAndPlaylists(implicit signer: SignerV2): F[Either[ErrorBody, (PrivateUser, Page[SimplePlaylist])]] = {
    val getUser = client.users.me.map(_.entity)
    val getUserPlaylists = client.users.getPlaylists(limit = 50).map(_.entity)

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
