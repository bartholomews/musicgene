package com.wrapper.spotify.entities

case class Page[T]
(
href: String,
items: List[T],
limit: Int,
next: Option[String],
offset: Int,
previous: Option[String],
total: Int
)