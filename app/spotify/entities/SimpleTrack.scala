package com.wrapper.spotify.entities

case class SimpleTrack
(
artists: List[SimpleArtist],
available_markets: List[String],  // ISO 3166-1 alpha-2 code
disc_number: Int = 1,
duration_ms: Int,
explicit: Boolean,
external_urls: (String, String),
href: String,
id: String,
is_playable: Boolean,
linked_from: TrackLink
)

case class TrackLink
(
external_urls: (String, String),
href: String,
id: String,
uri: String
) { val objectType = "track" }
