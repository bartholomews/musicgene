package views.spotify.responses

import java.util.UUID

case class GeneratedPlaylistResultId(value: UUID) extends AnyVal
object GeneratedPlaylistResultId {
  def apply(): GeneratedPlaylistResultId = new GeneratedPlaylistResultId(UUID.randomUUID())
}