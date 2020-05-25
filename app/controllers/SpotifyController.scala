package controllers

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.google.inject.Inject
import controllers.http.SpotifySession
import io.bartholomews.fsclient.entities.oauth.{AuthorizationCode, SignerV2}
import io.bartholomews.spotify4s.SpotifyClient
import io.bartholomews.spotify4s.entities._
import javax.inject._
import model.genetic.Playlist
import model.music.{MusicUtil, Song}
import org.http4s.Uri
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import views.spotify.PlaylistRequest

import scala.concurrent.ExecutionContext

/**
 *
 */
@Singleton
class SpotifyController @Inject() (cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractControllerIO(cc)
    with play.api.i18n.I18nSupport {

  import controllers.http.SpotifyHttpResults._

  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val spotifyClient: SpotifyClient = SpotifyClient.unsafeFromConfig()

  /**
   * The 'tracks' collection at injected MongoDB server
   */
  //  val dbTracks: Imports.MongoCollection = MongoController.getCollection(
  //    configuration.underlying.getString("mongodb.uri"),
  //    configuration.underlying.getString("mongodb.db"),
  //    configuration.underlying.getString("mongodb.tracks")
  //  )

  /**
   * Redirect a user to authenticate with Spotify and grant permissions to the application
   *
   * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
   */
  private def authenticate(request: Request[AnyContent]): Result =
    redirect {
      spotifyClient.auth.authorizeUrl(
        // TODO: load from config
        redirectUri = Uri.unsafeFromString(s"${requestHost(request)}/spotify/callback"),
        state = None,
        scopes = List.empty,
        showDialog = true
      )
    }

  def hello(): Action[AnyContent] = ActionIO.async { implicit request =>
    withToken { signer =>
      spotifyClient.users.me(signer).map(_.toResult(me => Ok(views.html.spotify.hello(me))))
    }
  }

  def callback: Action[AnyContent] = ActionIO.async { implicit request =>
    (for {
      uri <- EitherT.fromEither[IO](requestUri(request).leftMap(parseFailure => badRequest(parseFailure.details)))
      maybeToken <- EitherT.liftF(spotifyClient.auth.AuthorizationCode.fromUri(uri))
      authorizationCode <- EitherT.fromEither[IO](maybeToken.entity.leftMap(errorToResult))
      result <- EitherT.right[Result](
        spotifyClient.users
          .me(authorizationCode)
          .map(
            _.toResult(me =>
              Ok(views.html.spotify.hello(me))
                .addingToSession(SpotifySession.serializeSession(authorizationCode): _*)
            )
          )
      )
    } yield result).value.map(_.fold(identity, identity))
  }

  def logout(): Action[AnyContent] = ActionIO.async { implicit request =>
    IO.pure(
      Ok(views.html.index())
        .removingFromSession(SpotifySession.accessSessionKey)
        .removingFromSession(SpotifySession.refreshSessionKey)
    )
  }

  private def refresh(f: SignerV2 => IO[Result])(implicit request: Request[AnyContent]): IO[Result] =
    SpotifySession
      .getRefreshSession(request)
      .fold(IO.pure(authenticate(request)))(token =>
        spotifyClient.auth.AuthorizationCode
          .refresh(token)
          .flatMap(_.toResultF { authorizationCode =>
            f(authorizationCode).map(_.addingToSession(SpotifySession.serializeSession(authorizationCode): _*))
          })
      )

  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  def withToken[A](f: SignerV2 => IO[Result])(implicit request: Request[AnyContent]): IO[Result] =
    SpotifySession.getAccessSession(request) match {
      case None => IO.pure(authenticate(request))
      case Some(accessToken: AuthorizationCode) =>
        if (accessToken.isExpired()) refresh(f)
        else f(accessToken)
    }

  /**
   * @param userId the ID of the logged-in user
   * @return the FIRST PAGE of a user playlists TODO
   */
  def playlists(userId: SpotifyUserId): Action[AnyContent] = ActionIO.async { implicit request =>
    withToken { accessToken =>
      spotifyClient.playlists
        .getUserPlaylists(userId)(accessToken)
        .map(_.toResult(pg => Ok(views.html.spotify.playlists("Playlists", pg.items))))
    }
  }

  def songs(playlistId: SpotifyId): Action[AnyContent] = ActionIO.async { implicit request =>
    withToken { implicit accessToken =>
      spotifyClient.playlists
        .getPlaylist(playlistId)
        .flatMap(_.toResultF { playlist =>
          val tracksPage: List[FullTrack] = playlist.tracks.items.map(_.track)

          val audioFeaturesLookup: IO[Map[SpotifyId, AudioFeatures]] =
            spotifyClient.tracks
              .getAudioFeatures(tracksPage.map(_.id).toSet)
              .map(
                _.entity.fold(
                  _ => Map.empty,
                  af => af.map(f => Tuple2(f.id, f)).toMap
                  // MongoController.writeToDB(dbTracks, song) // TODO only if not already there
                )
              )

          audioFeaturesLookup.map { lookup =>
            val songs: List[Song] = tracksPage.map { track =>
              lookup.get(track.id).fold(MusicUtil.toSong(track))(af => MusicUtil.toSong(track, af))
            }

            Ok(views.html.spotify.tracks(playlist, songs, PlaylistRequest.form))
          }
        })
    }
  }

  //  /**
  //    * Retrieve the first 200 tracks stored in the MongoDB database,
  //    * and return them rendering the view tracks.scala.html
  //    *
  //    * @return an HTTP Ok 200 on tracks view with 200 Song instances retrieved from MongoDB
  //    */
  //  def sampleTracks = Action {
  //    // retrieve 200 sample tracks from MongoDB
  //    val songs = MongoController.read(dbTracks, 200)
  //    Ok(views.html.tracks("sample",
  //      Vector(("A list of unsorted tracks with different characteristics", songs)))
  //    )
  //  }

  /*
  private def toSong(playlist_id: String): Future[List[(Option[String], List[Song])]] = {
    val f: List[Future[Song]] = page.items.map(pt =>
      tracksApi.getAudioFeatures(pt.track.id.get) map {
        af => MusicUtil.toSong(pt.track, af)
      })
    api.getFutureList[Song](f)
  }
   */

  //  private def getPlaylistSongs(playlist_href: String): Future[List[Song]] = {
  //    api.get(playlist_href) flatMap { page =>
  //      val f: List[Future[Song]] = page.items.map(pt =>
  //        tracksApi.getAudioFeatures(pt.track.id.get) map {
  //          af => MusicUtil.toSong(pt.track, af)
  //        })
  //      api.getFutureList[Song](f)
  //    }
  //  }

  //  private def getSong(href: String): Future[Song] = {
  //    tracksApi.getPlaylistTracks(href) flatMap { page =>
  //      val playlist = page.items.head
  //      tracksApi.getAudioFeatures(page.items.head.track.id.get) map {
  //        af => MusicUtil.toSong(page.items.head.track, af)
  //      }
  //    }
  //  }

  /*
  def songs(href: String): Action[AnyContent] = handleAsync {
    tracksApi.getPlaylistTracks(href) map {
      t => {
        val f: String = for {
          tracks <- t.items.map(pt => pt.track)
          af <- tracksApi.getAudioFeatures(tracks.id.get)
        } yield (tracks, af)
        Ok(views.html.tracks("Some playlist", ("", f) :: Nil))
      }
    }
  }
   */

  // def newReleases: Future[List[SimpleAlbum]] = spotify.browse.newReleases

  /**
   * Retrieve a playlist's underlying tracks' audio attributes.
   *
   * @return a SimplePlaylist with associated sequence of Song instances
   *         wrapped in an Option, if an error occurred None is returned
   */
  //  def getPlaylistCollection(userId: String, playlistId: String): Option[(SimplePlaylist, Vector[Song])] = {
  //    try {
  //      // get each playlist's tracks (only ids and basic access data)
  //      val trackList: Future[Page[PlaylistTrack]] = playlistsApi.tracks(userId, playlistId)
  //      // retrieve those which are already stored in Redis cache
  //      val inCache: Vector[Song] = trackList.flatMap(t => cache.get[Song](t.))
  //      // tracks not stored in cache but stored in MongoDB
  //      val inDB: Vector[Song] = trackList.filterNot(t => inCache.exists(s => s.id == t.getId))
  //        .flatMap(t => MongoController.readByID(dbTracks, t.getId))
  //      inDB.foreach(s => cache.set(s.id, s))
  //      val retrieved = inCache ++ inDB
  //      // create a sequence of Song with those retrieved from MongoDB
  //      // and getting the others from the Spotify API
  //      val outDB: Vector[Song] = MusicUtil.toSongs(
  //        trackList.filterNot(t => retrieved.exists(s => s.id == t.getId))
  //          .flatMap(t => spotify.getAnalysis(t.getId) match {
  //            case None => None
  //            case Some(analysis) => Some(t, analysis)
  //          }
  //          ))
  //      outDB.foreach(s => {
  //        // write to MongoDB those Song instances which weren't there
  //        MongoController.writeToDB(dbTracks, outDB)
  //        // write to Redis cache
  //        cache.set(s.id, s)
  //      })
  //      // return the playlist and its tracks
  //      Some(playlist, retrieved ++ outDB)
  //    } catch {
  //      // return None if an error occurred during the operation
  //      case _: NullPointerException => None
  //      case _: BadRequestException => None
  //    }
  //  }

  //
  /**
   * @see https://www.playframework.com/documentation/latest/ScalaJsonHttp
   * Handle a JSON HTTP request with a Content-type header as text/json or application/json
   * The body of the request is parsed by the `JSONParser` object
   * which will return an Option[PlaylistRequest].
   * If the request is parsed correctly, the playlist generation algorithm
   * will be started with the data from the PlaylistRequest
   * and a 200 Ok with the JSON response will return,
   * else a 400 Bad Request
   *
   * @return a 200 Ok with a JSON response (name of the Playlist and Array of IDs)
   *         or a 400 Bad Request if the request couldn't be parsed
   */
//  def generatePlaylist: Action[JsValue] = Action(parse.json) { implicit request =>
//    println("~" * 50)
//    println(request.body)
//    println("~" * 50)
//    JSONParser.parseRequest(request.body) match {
//      // the request is not parsed correctly: return an HTTP 400 Bad Request
//      case None    => BadRequest("Json Request failed")
//      case Some(p) =>
////        val db = getFromRedisThenMongo(p)
//        // call the playlist generation algorithm with (MusicCollection, Set[Constraint], Int) args
////        val playlist = GA.generatePlaylist(db, p.constraints, p.length)
//        // the JSON response with the playlist name and the returned playlist
//        val js = createJsonResponse(p.name, new Playlist(Vector.empty, CostBasedFitness(Set.empty)))
//        Ok(js)
//    }
//  }

  def renderGeneratedPlaylist(requestId: String): Action[AnyContent] = ActionIO.async { implicit request =>
    IO.pure(Ok(views.html.spotify.playlist_generation(requestId)))
  }

  val generatePlaylist: Action[PlaylistRequest] = Action(parse.form(PlaylistRequest.form)) { implicit request =>
    val playlistRequest = request.body
    Redirect(routes.SpotifyController.renderGeneratedPlaylist(playlistRequest.name))
  }

  /**
   * The JSON response to send back to the user
   *
   * @param name the name of the playlist
   * @param playlist the Playlist generated
   * @return a JSON with the name of the playlist and an array of tracks ids
   */
  def createJsonResponse(name: String, playlist: Playlist): JsValue = {
    // map each Song in the playlist back to only its id
    // as the view is supposed to reconstruct the response
    val tracksID = Json.toJson(playlist.songs.map(s => s.id))
    Json.obj(
      "name" -> name, // a String name of the playlist
      "ids" -> tracksID // an Array with the tracks IDs
    )
  }
}
