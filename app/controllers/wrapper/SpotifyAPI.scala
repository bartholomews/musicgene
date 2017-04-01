package controllers.wrapper

import javax.inject.Inject

import logging.AccessLogging
import controllers.wrapper.entities.Page
import play.api.libs.json.Reads
import play.api.libs.ws.WSClient
import play.api.mvc.Controller

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class SpotifyAPI @Inject()(base_api: BaseApi, profiles_endpoint: ProfilesApi, browse_endpoint: BrowseApi,
                           playlists_endpoint: PlaylistsApi, tracks_endpoint: TracksApi,
                           ws: WSClient) extends Controller with AccessLogging {

  final val api: BaseApi = base_api
  final val browse: BrowseApi = browse_endpoint
  final val profiles: ProfilesApi = profiles_endpoint
  final val playlists: PlaylistsApi = playlists_endpoint
  final val tracks: TracksApi = tracks_endpoint

}
