package io.bartholomews.musicgene.controllers.http.session

import enumeratum._

sealed trait SpotifySessionUser extends EnumEntry

object SpotifySessionUser extends PlayEnum[SpotifySessionUser] {

  val values: IndexedSeq[SpotifySessionUser] = findValues

  case object Main   extends SpotifySessionUser
  case object Source extends SpotifySessionUser
}
