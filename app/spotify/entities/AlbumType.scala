package com.wrapper.spotify.entities

sealed trait AlbumType {

  case object Album extends AlbumType

  case object Single extends AlbumType

  case object Compilation extends AlbumType

}