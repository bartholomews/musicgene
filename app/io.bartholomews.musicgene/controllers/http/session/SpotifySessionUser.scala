package io.bartholomews.musicgene.controllers.http.session

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait SpotifySessionUser extends EnumEntry with Lowercase

object SpotifySessionUser extends PlayLowercaseEnum[SpotifySessionUser] {

  val values: IndexedSeq[SpotifySessionUser] = findValues

  case object Main extends SpotifySessionUser
  case object Source extends SpotifySessionUser
}
