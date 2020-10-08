package views.spotify.responses

import io.bartholomews.spotify4s.entities.{FullPlaylist, Page, PrivateUser, SimplePlaylist}

case class SpotifyUserAndPlaylists(user: PrivateUser, playlists: List[SimplePlaylist])

object SpotifyUserAndPlaylists {
  // FIXME: Not sure, maybe should be simple playlist after all, and fetch just tracks
  //  for playlists you want to migrate
  type MainUserAndSimplePlaylists = (PrivateUser, Page[SimplePlaylist])
  type SrcUserAndFullPlaylists = (PrivateUser, Page[SimplePlaylist]) // FullPlaylist
}