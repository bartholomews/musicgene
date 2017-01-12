package model.entities

case class AudioFeatures
(
  acousticness: Float,
  analysis_url: String,
  danceability: Float,
  duration_ms: Float,
  energy: Float,
  id: String,
  instrumentalness: Float,
  key: Int,
  liveness: Float,
  loudness: Float,
  mode: Int,
  speechiness: Float,
  tempo: Float,
  time_signature: Int,
  track_href: String,
  uri: String,
  valence: Float
) { val objectType = "audio_features" }
