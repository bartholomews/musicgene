package model.entities

case class Album
(
album_type: AlbumType,
artists: List[SimpleArtist],
available_markets: List[String],
copyrights: List[(String, String)],
external_ids: (String, String),
external_urls: (String, String),
genres: List[String],
href: String,
id: String,
images: List[Image],
label: String,
name: String,
popularity: Int,
release_date: String,
release_date_precision: String,
tracks: Page[SimpleTrack],
uri: String
) { val objectType = "album" }
