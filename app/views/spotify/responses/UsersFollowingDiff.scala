package views.spotify.responses

import io.bartholomews.spotify4s.core.entities.PrivateUser
import io.bartholomews.spotify4s.core.entities.SpotifyId.SpotifyArtistId

final case class UsersFollowingDiff(main: PrivateUser, src: PrivateUser, diff: List[MaybeFollowingArtist])

final case class MaybeFollowingArtist(artistId: SpotifyArtistId, isFollowing: Boolean)
object MaybeFollowingArtist {
  def apply(tuple: (SpotifyArtistId, Boolean)): MaybeFollowingArtist = tuple match {
    case (artistId, isFollowing) => MaybeFollowingArtist(artistId, isFollowing)
  }
}
