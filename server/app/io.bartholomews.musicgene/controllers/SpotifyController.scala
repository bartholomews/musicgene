package io.bartholomews.musicgene.controllers

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.google.inject.Inject
import eu.timepit.refined.api.Refined
import io.bartholomews.fsclient.entities.oauth.{AuthorizationCode, SignerV2}
import io.bartholomews.musicgene.controllers.http.SpotifyCookies
import io.bartholomews.musicgene.model.genetic.GA
import io.bartholomews.musicgene.model.music.{MusicCollection, MusicUtil}
import io.bartholomews.spotify4s.SpotifyClient
import io.bartholomews.spotify4s.api.SpotifyApi.{Limit, Offset}
import io.bartholomews.spotify4s.entities.{AudioFeatures, FullTrack, SpotifyId, SpotifyUserId}
import javax.inject._
import org.http4s.Uri
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import views.spotify.requests.PlaylistRequest
import views.spotify.responses.{GeneratedPlaylist, GeneratedPlaylistResultId}

import scala.concurrent.ExecutionContext

/**
  *
  */
@Singleton
class SpotifyController @Inject()(cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends AbstractControllerIO(cc)
    with play.api.i18n.I18nSupport {

  import eu.timepit.refined.auto.autoRefineV
  import io.bartholomews.musicgene.controllers.http.SpotifyHttpResults._
  import io.bartholomews.musicgene.model.helpers.CollectionsHelpers._

  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val spotifyClient: SpotifyClient = SpotifyClient.unsafeFromConfig()

  /**
    * Redirect a user to authenticate with Spotify and grant permissions to the application
    *
    * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
    */
  private def authenticate(request: Request[AnyContent]): Result =
    redirect {
      spotifyClient.auth.authorizeUrl(
        // TODO: load from config
        redirectUri =
          Uri.unsafeFromString(s"${requestHost(request)}/spotify/callback"),
        state = None,
        scopes = List.empty,
        showDialog = true
      )
    }

  def hello(): Action[AnyContent] = ActionIO.async { implicit request =>
    withToken { signer =>
      spotifyClient.users
        .me(signer)
        .map(_.toResult(me => Ok(views.html.spotify.hello(me))))
    }
  }

  def callback: Action[AnyContent] = ActionIO.async { implicit request =>
    (for {
      uri <- EitherT.fromEither[IO](
        requestUri(request)
          .leftMap(parseFailure => badRequest(parseFailure.details))
      )
      maybeToken <- EitherT.liftF(
        spotifyClient.auth.AuthorizationCode.fromUri(uri)
      )
      authorizationCode <- EitherT.fromEither[IO](
        maybeToken.entity.leftMap(errorToResult)
      )
      result <- EitherT.right[Result](
        spotifyClient.users
          .me(authorizationCode)
          .map(
            _.toResult(
              me =>
                Ok(views.html.spotify.hello(me))
                  .withCookies(SpotifyCookies.accessCookies(authorizationCode): _*)
            )
          )
      )
    } yield result).value.map(_.fold(identity, identity))
  }

  def logout(): Action[AnyContent] = ActionIO.async { implicit request =>
    IO.pure(
      Ok(views.html.index2("FIXME"))
        .discardingCookies(SpotifyCookies.discardCookies: _*)
    )
  }

  private def refresh(
    f: SignerV2 => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    SpotifyCookies
      .extractRefreshToken(request)
      .fold(IO.pure(authenticate(request)))(
        token =>
          spotifyClient.auth.AuthorizationCode
            .refresh(token)
            .flatMap(_.toResultF { authorizationCode =>
              f(authorizationCode).map(
                _.withCookies(
                  SpotifyCookies.accessCookies(authorizationCode): _*
                )
              )
            })
      )

  // http://pauldijou.fr/jwt-scala/samples/jwt-play/
  def withToken[A](
    f: SignerV2 => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    SpotifyCookies.extractAuthCode(request) match {
      case None => IO.pure(authenticate(request))
      case Some(accessToken: AuthorizationCode) =>
        if (accessToken.isExpired()) refresh(f)
        else f(accessToken)
    }

  /**
    * @param userId the Spotify id of the logged-in user
    * @return the FIRST PAGE of a user playlists TODO
    */
  def playlists(userId: SpotifyUserId, page: Int): Action[AnyContent] =
    ActionIO.async { implicit request =>
      withToken { accessToken =>
        {
          val pageLimit: Limit = 50
          val pageOffset: Offset = Refined.unsafeApply((page - 1) * pageLimit.value)
          spotifyClient.playlists
            .getUserPlaylists(
              userId = userId,
              limit = pageLimit,
              offset = pageOffset
            )(accessToken)
            .map(
              _.toResult(
                pg =>
                  Ok(views.html.spotify.playlists("Playlists", pg.items, page))
              )
            )
        }
      }
    }

  def tracks(playlistId: SpotifyId): Action[AnyContent] = ActionIO.async {
    implicit request =>
      withToken { implicit accessToken =>
        spotifyClient.playlists
          .getPlaylist(playlistId)
          .map(_.toResult { playlist =>
            val tracks: List[FullTrack] = playlist.tracks.items.map(_.track)
            Ok(views.html.spotify.tracks(playlist.tracks.copy(items = tracks)))
          })
      }
  }

  val generatePlaylist: Action[JsValue] = ActionIO.async[JsValue](parse.json) {
    implicit request =>
      val playlistRequestJson = request.body.validate[PlaylistRequest]
      playlistRequestJson.fold(
        errors =>
          IO.pure(
            BadRequest(
              Json.obj(
                "error" -> "invalid_playlist_request_payload",
                "message" -> JsError.toJson(errors)
              )
            )
        ),
        playlistRequest =>
          // val db = getFromRedisThenMongo(p)
          //        Ok(Json.toJson(PlaylistResponse.fromPlaylist(p)))
          generatePlaylist(playlistRequest)(request.map(AnyContentAsJson))
      )
  }

  private def generatePlaylist(
    playlistRequest: PlaylistRequest
  )(implicit request: Request[AnyContent]): IO[Result] =
    withToken { implicit accessToken =>
      val getTracks: IO[Either[Result, List[FullTrack]]] =
        playlistRequest.tracks
          .groupedNes(size = 50)
          .map(
            xs =>
              spotifyClient.tracks
                .getTracks(xs, market = None)
                .map(_.entity.leftMap(errorToJsonResult))
          )
          .parSequence
          .map(_.sequence.map(_.flatten))

      val getAudioFeatures: IO[Either[Result, List[AudioFeatures]]] =
        playlistRequest.tracks
          .groupedNes(size = 100)
          .map(
            xs =>
              spotifyClient.tracks
                .getAudioFeatures(xs)
                .map(_.entity.leftMap(errorToJsonResult))
          )
          .parSequence
          .map(_.sequence.map(_.flatten))

      Tuple2(getTracks, getAudioFeatures).parMapN({
        case (getTracksResult, getAudioFeaturesResult) =>
          (for {
            // MongoController.writeToDB(dbTracks, song) // TODO only if not already there
            audioFeaturesLookup <- getAudioFeaturesResult
              .map(af => af.map(f => Tuple2(f.id, f)).toMap)
            spotifyTracks <- getTracksResult
            audioTracks = spotifyTracks.map { track =>
              track.id.fold(MusicUtil.toAudioTrack(track)) { trackId =>
                audioFeaturesLookup
                  .get(trackId)
                  .fold(MusicUtil.toAudioTrack(track))(
                    af => MusicUtil.toAudioTrack2(track, af)
                  )
              }
            }
            playlist = GA.generatePlaylist(
              db = new MusicCollection(audioTracks),
              c = playlistRequest.constraints.map(_.toDomain),
              playlistRequest.length
            )
          } yield
            Ok(
              Json.toJson(
                GeneratedPlaylist.fromPlaylist(playlistRequest.name, playlist)
              )
            ))
            .fold(identity, identity)
      })
    }

  def renderGeneratedPlaylist(
    generatedPlaylistResultId: GeneratedPlaylistResultId
  ): Action[AnyContent] =
    ActionIO.async { implicit request =>
      // TODO could store previously generated playlist results
      IO.pure(
        Ok(
          views.html.spotify
            .playlist_generation(generatedPlaylistResultId, List.empty)
        )
      )
    }

  /**
    * The 'tracks' collection at injected MongoDB server
    */
  //  val dbTracks: Imports.MongoCollection = MongoController.getCollection(
  //    configuration.underlying.getString("mongodb.uri"),
  //    configuration.underlying.getString("mongodb.db"),
  //    configuration.underlying.getString("mongodb.tracks")
  //  )

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

//  val generatePlaylistForm: Action[PlaylistRequest] = Action(parse.form(PlaylistRequest.form)) { implicit request =>
//    val playlistRequest = request.body
//    println(playlistRequest)
//    val p = GA.generatePlaylist(
//      db = new MusicCollection(songs = Vector.empty),
//      c = Set.empty,
//      playlistRequest.range.getOrElse(0)
//    )
//    Redirect(routes.SpotifyController.renderGeneratedPlaylist(playlistRequest.name))
//  }
}
