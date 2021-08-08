package views.spotify.requests

sealed trait PlaylistsAction {
  def label: String
  def userType: String
}

case object MigratePlaylists extends PlaylistsAction {
  override val label = "migrate"
  override val userType = "source"
}

case object UnfollowPlaylists extends PlaylistsAction {
  override val label = "unfollow"
  override val userType = "main"
}