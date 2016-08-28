package controllers

import com.wrapper.spotify.models.{SimplePlaylist, Track}
import model.music.{Cache, MusicUtil, Song}

import scala.collection.JavaConversions._
/**
  *
  */
class SpotifyAdapter {
  val spotify = SpotifyJavaController.getInstance()

  // TODO rate limits...
  def getPlaylistCollection(playlist: SimplePlaylist): (SimplePlaylist, Vector[Song]) = {
    println("creating playlist collection..")
    val trackList: Vector[Track] = spotify.getPlaylistTracks(playlist).map(t => t.getTrack).toVector
    val (inCache, outCache) = Cache.getFromCache(trackList.map(t => t.getId))
    (playlist, inCache ++ MusicUtil.toSongs(
      trackList.filter(t => outCache.contains(t.getId))
        .map { t => (t, spotify.getAnalysis(t.getId)) }
    ))
  }

}
