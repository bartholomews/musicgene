package com.wrapper.spotify.entities

case class SimpleArtist
(
external_urls: (String, String),
href: String,
id: String,
name: String,
uri: String
) { val objectType = "artist" }