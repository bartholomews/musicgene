package views.spotify.responses

import io.bartholomews.spotify4s.entities.{Page, PrivateUser, SimplePlaylist}

case class SpotifyUserAndPlaylists(user: PrivateUser, playlists: List[SimplePlaylist])

object SpotifyUserAndPlaylists {
  type TheTuple = (PrivateUser, Page[SimplePlaylist])
}