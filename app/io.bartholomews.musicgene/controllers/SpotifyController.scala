package io.bartholomews.musicgene.controllers

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.google.inject.Inject
import eu.timepit.refined.api.Refined
import io.bartholomews.fsclient.entities.ErrorBody
import io.bartholomews.fsclient.entities.oauth.{AuthorizationCode, SignerV2}
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import io.bartholomews.musicgene.controllers.http.{SpotifyCookies, SpotifySessionKeys}
import io.bartholomews.musicgene.model.genetic.GA
import io.bartholomews.musicgene.model.music._
import io.bartholomews.spotify4s.SpotifyClient
import io.bartholomews.spotify4s.api.SpotifyApi.{Limit, Offset}
import io.bartholomews.spotify4s.entities._
import javax.inject._
import org.http4s.Uri
import play.api.Logging
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import views.spotify.requests.{PlaylistMigrationRequest, PlaylistRequest, PlaylistsMigrationRequest, PlaylistsUnfollowRequest}
import views.spotify.responses._

import scala.concurrent.ExecutionContext

/**
 *
 */
@Singleton
class SpotifyController @Inject()(cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends AbstractControllerIO(cc)
  with Logging
  with play.api.i18n.I18nSupport {

  import eu.timepit.refined.auto.autoRefineV
  import io.bartholomews.musicgene.controllers.http.SpotifyHttpResults._
  import io.bartholomews.musicgene.model.helpers.CollectionsHelpers._

  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val spotifyClient: SpotifyClient = SpotifyClient.unsafeFromConfig()

  def authenticate(session: SpotifySessionUser): Action[AnyContent] = ActionIO.async { implicit request =>
    logger.info(s"Authenticating session $session")
    IO.pure(authenticate(request.addAttr(SpotifySessionKeys.spotifySessionUser, session)))
  }

  /**
   * Redirect a user to authenticate with Spotify and grant permissions to the application
   *
   * @return a Redirect Action (play.api.mvc.Action type is a wrapper around the type `Request[A] => Result`,
   */
  private def authenticate(implicit request: Request[AnyContent]): Result = {
    val maybeSession = request.attrs.get(SpotifySessionKeys.spotifySessionUser)
    logger.info(s"Authenticate request session: $maybeSession")
    maybeSession
      .map { session =>
        println(session.entryName)
        redirect(
          spotifyClient.auth.authorizeUrl(
            // TODO: load from config
            redirectUri = Uri.unsafeFromString(s"${requestHost(request)}/spotify/${session.entryName}/callback"),
            state = None,
            scopes = List(
              // FIXME: Scope based on main/src
              SpotifyScope.PLAYLIST_MODIFY_PRIVATE,
              SpotifyScope.PLAYLIST_MODIFY_PUBLIC,
              SpotifyScope.PLAYLIST_READ_PRIVATE,
              SpotifyScope.PLAYLIST_READ_COLLABORATIVE
            ),
            showDialog = true
          )
        )
      }
      .getOrElse(InternalServerError("Something went wrong handling spotify session, please contact support."))
  }

  def hello(): Action[AnyContent] = ActionIO.asyncWithMainUser { implicit request =>
    withToken { signer =>
      logger.info("hello")
      spotifyClient.users
        .me(signer)
        .map(_.toResult(me => Ok(views.html.spotify.hello(me))))
    }
  }

  def callback(session: SpotifySessionUser): Action[AnyContent] = ActionIO.asyncWithSession(session) {
    implicit request =>
      (for {
        _ <- EitherT.pure[IO, String](logger.debug(s"Callback for session: $session"))
        uri <- EitherT.fromEither[IO](requestUri(request).leftMap(parseFailure => parseFailure.details))
        authorizationCode <- EitherT(
          spotifyClient.auth.AuthorizationCode.fromUri(uri).map(_.entity.leftMap(errorToString))
        )
      } yield Redirect(
        session match {
          case SpotifySessionUser.Main => routes.SpotifyController.hello()
          case SpotifySessionUser.Source => routes.SpotifyController.migrate()
        }
      ).withCookies(SpotifyCookies.accessCookies(authorizationCode): _*)).value.map(_.fold(errorString => {
        logger.error(errorString)
        Redirect(routes.HomeController.index())
      }, identity))
  }

  def logout(session: SpotifySessionUser): Action[AnyContent] = ActionIO.async { implicit request =>
    logger.debug(s"Logout session $session")
    IO.pure(session match {
      case SpotifySessionUser.Main =>
        Redirect(routes.HomeController.index())
          .discardingCookies(SpotifyCookies.discardAllCookies: _*)
      case SpotifySessionUser.Source =>
        Redirect(routes.SpotifyController.migrate())
          .discardingCookies(SpotifyCookies.discardCookies(session): _*)
    })
  }

  private def refresh(f: SignerV2 => IO[Result])(implicit request: Request[AnyContent]): IO[Result] =
    SpotifyCookies
      .extractRefreshToken(request)
      .fold(IO.pure(authenticate(request)))(token =>
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
  def withToken[A](f: SignerV2 => IO[Result])(implicit request: Request[AnyContent]): IO[Result] =
    SpotifyCookies.extractAuthCode(request) match {
      case None => IO.pure(authenticate(request))
      case Some(accessToken: AuthorizationCode) =>
        if (accessToken.isExpired()) refresh(f)
        else f(accessToken)
    }

  def withSourceUserToken[A, R](f: SignerV2 => IO[Result])(implicit request: Request[AnyContent]): IO[Option[Result]] =
    SpotifyCookies.extractAuthCode(SpotifySessionUser.Source) match {
      case None => IO.pure(None)
      case Some(accessToken: AuthorizationCode) =>
        if (accessToken.isExpired())
          refresh(f)(request.addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Source))
            .map(_.some)
        else f(accessToken).map(_.some)
    }

  /**
   * @return the FIRST PAGE of a user playlists TODO
   */
  def playlists(session: SpotifySessionUser, page: Int): Action[AnyContent] =
    ActionIO.asyncWithSession(session) { implicit request =>
      withToken { accessToken =>
        val pageLimit: Limit = 50
        val pageOffset: Offset = Refined.unsafeApply((page - 1) * pageLimit.value)
        spotifyClient.users
          .getPlaylists(limit = pageLimit, offset = pageOffset)(accessToken)
          .map(_.toResult(pg => Ok(views.html.spotify.playlists("Playlists", pg.items, page))))
      }
    }

  def tracks(session: SpotifySessionUser, playlistId: SpotifyId): Action[AnyContent] =
    ActionIO.asyncWithSession(session) { implicit request =>
      withToken { implicit accessToken =>
        spotifyClient.playlists
          .getPlaylist(playlistId)
          .map(_.toResult { playlist =>
            val tracks: List[FullTrack] = playlist.tracks.items.map(_.track)
            Ok(views.html.spotify.tracks(playlist.tracks.copy(items = tracks)))
          })
      }
    }

  // FIXME: Add sessionNumber to request
  val generatePlaylist: Action[JsValue] = ActionIO.async[JsValue](parse.json) { implicit request =>
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
        generatePlaylist(playlistRequest)(
          request
            .addAttr(SpotifySessionKeys.spotifySessionUser, SpotifySessionUser.Main)
            .map(AnyContentAsJson)
        )
    )
  }

  private def generatePlaylist(
                                playlistRequest: PlaylistRequest
                              )(implicit request: Request[AnyContent]): IO[Result] =
    withToken { implicit accessToken =>
      val getTracks: IO[Either[Result, List[FullTrack]]] =
        playlistRequest.tracks
          .groupedNes(size = 50)
          .map(xs =>
            spotifyClient.tracks
              .getTracks(xs, market = None)
              .map(_.entity.leftMap(errorToJsonResult))
          )
          .parSequence
          .map(_.sequence.map(_.flatten))

      val getAudioFeatures: IO[Either[Result, List[AudioFeatures]]] =
        playlistRequest.tracks
          .groupedNes(size = 100)
          .map(xs =>
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
                  .fold(MusicUtil.toAudioTrack(track))(af => MusicUtil.toAudioTrack2(track, af))
              }
            }
            playlist = GA.generatePlaylist(
              db = new MusicCollection(audioTracks),
              c = playlistRequest.constraints.map(_.toDomain),
              playlistRequest.length
            )
          } yield Ok(Json.toJson(GeneratedPlaylist.fromPlaylist(playlistRequest.name, playlist))))
            .fold(identity, identity)
      })
    }

  def renderGeneratedPlaylist(generatedPlaylistResultId: GeneratedPlaylistResultId): Action[AnyContent] =
    ActionIO.async { implicit request =>
      // TODO could store previously generated playlist results
      IO.pure(
        Ok(
          views.html.spotify
            .playlist_generation(generatedPlaylistResultId, List.empty)
        )
      )
    }

  def getUserAndPlaylists(implicit request: Request[AnyContent], signer: SignerV2): IO[Either[ErrorBody, (PrivateUser, Page[SimplePlaylist])]] = {

    val getUser = spotifyClient.users.me.map(_.entity)
    val getUserPlaylists = spotifyClient.users.getPlaylists(limit = 50).map(_.entity)

    (getUser, getUserPlaylists)
      .parMapN({
        case (aaa, bbb) =>
          for {
            privateUser <- aaa
            playlists <- bbb
          } yield Tuple2(privateUser, playlists)
      })
  }

  private def getSourceUserForMigration(mainUser: (PrivateUser, Page[SimplePlaylist]))(implicit request: Request[AnyContent]): IO[Result] =
    withSourceUserToken { implicit signer =>
      logger.info("getSourceUserForMigration")
      getUserAndPlaylists.map(
        _.toResulto(entity => Ok(views.html.spotify.migrate(Right(Tuple2(mainUser, Some(entity))))))
      )
    }.map(_.getOrElse(Ok(views.html.spotify.migrate(Right(Tuple2(mainUser, None))))))

  private def getMainUserAndPlaylists(
                                       andThen: ((PrivateUser, Page[SimplePlaylist])) => IO[Result]
                                     )(implicit request: Request[AnyContent]): IO[Result] =
    withToken { implicit signer =>
      getUserAndPlaylists.flatMap(
        _.fold(
          _ => Ok(views.html.spotify.migrate(Left("Something went wrong while fetching the main user"))).pure[IO],
          entity => andThen(entity)
        )
      )
    }

  def migrate(): Action[AnyContent] = ActionIO.asyncWithMainUser { implicit request =>
    withToken { implicit signer =>
      logger.info("migrate")
      getMainUserAndPlaylists(getSourceUserForMigration)
    }
  }

  def wwww(mainUserId: SpotifyUserId,
           playlistRequest: PlaylistMigrationRequest,
           playlistToClone: FullPlaylist)(implicit request: Request[AnyContent]): IO[Result] = {

    withToken {
      implicit mainUserToken =>
        spotifyClient.playlists
          .createPlaylist(
            userId = mainUserId,
            playlistName = playlistRequest.name,
            public = playlistRequest.public,
            collaborative = playlistRequest.collaborative,
            description = playlistRequest.description
          )
          .flatMap(newPlaylistResponse => {

            SpotifyUri.fromList(playlistToClone.tracks.items
              .filterNot(_.isLocal) // FIXME: :( maybe should add to the response to inform the user
              .map(_.track.uri)).fold(
              err => IO.pure(BadRequest(err)),
              uris =>
                newPlaylistResponse.toResultF { newPlaylist =>
                  spotifyClient.playlists.addTracksToPlaylist(
                    playlistId = newPlaylist.id,
                    uris = uris, // FIXME: Need to get ALL pages...
                    position = None
                  ).map(wee => wee.foldBody(
                    err => errorToResult(err),
                    _ => Ok("Playlist have been migrated.")
                  ))
                }
            )
          })
    }
  }

  def unfollowPlaylistsPPP(mainUserId: SpotifyUserId, playlistsToUnfollow: List[SpotifyId])(implicit request: Request[AnyContent]): IO[Result] = {
    withToken { implicit token =>
      playlistsToUnfollow.map(spotifyClient.follow.unfollowPlaylist).parSequence
        .map((re: List[HttpResponse[Unit]]) => {
          val (a, b) = re.partition(result => result.status.isSuccess)
          Ok(Json.obj(
            ("success", a.size),
            ("failures", b.size)
          ))
        })
    }
  }

  // Would be nice to give feedback while loading spinner, e.g. "Migrating playlist xxx..."
  // https://www.playframework.com/documentation/2.8.x/ScalaWebSockets
  def migratePPP(mainUserId: SpotifyUserId, playlistRequest: PlaylistMigrationRequest)(
    implicit request: Request[AnyContent]
  ): IO[Result] = {

    withSourceUserToken(sourceUserToken =>
      // FIXME: Shuold probably use getPlaylistFields(/tracks)
      spotifyClient.playlists
        .getPlaylist(
          playlistId = playlistRequest.id,
          market = None
        )(sourceUserToken)
        .flatMap(_.toResultF(playlistToClone => wwww(mainUserId, playlistRequest, playlistToClone)))
    )
      .map(_.getOrElse(BadRequest("No main user found in cookies")))
  }

  val unfollowPlaylists: Action[JsValue] = ActionIO.jsonAsyncWithMainUser { implicit request =>
    val unfollowPlaylistsRequest = request.body.validate[PlaylistsUnfollowRequest]
    unfollowPlaylistsRequest.fold(
      errors =>
        IO.pure(
          BadRequest(
            Json.obj(
              "error" -> "invalid_playlist_request_payload",
              "message" -> JsError.toJson(errors)
            )
          )
        ),
      playlistRequests => {
        logger.debug(s"user => ${playlistRequests.userId.toString}")
        playlistRequests.playlists.foreach { pr =>
          logger.debug(pr.toString)
        }

        unfollowPlaylistsPPP(playlistRequests.userId, playlistRequests.playlists.map(_.id))(request.map(AnyContent.apply))
      }
    )
  }

  val migratePlaylists: Action[JsValue] = ActionIO.jsonAsyncWithMainUser { implicit request =>
    val playlistIdsRequest = request.body.validate[PlaylistsMigrationRequest]
    playlistIdsRequest.fold(
      errors =>
        IO.pure(
          BadRequest(
            Json.obj(
              "error" -> "invalid_playlist_request_payload",
              "message" -> JsError.toJson(errors)
            )
          )
        ),
      playlistRequests => {
        logger.debug(s"user => ${playlistRequests.userId.toString}")
        playlistRequests.playlists.foreach { pr =>
          logger.debug(pr.toString)
        }


        playlistRequests.playlists.map(p => {
          migratePPP(
            playlistRequests.userId,
            p
          )(request.map(AnyContent.apply))
        }).sequence.map(re => {
          // Ior?
          val (a, b) = re.partition(result => result.header.status == 200)
          Ok(Json.obj(
            ("success", a.size),
            ("failures", b.size)
          ))
        })
      }
    )
  }
}

// TODO:
//  -> Remove duplicates (two playlists with same name and exactly same tracks)
//  -> Remove all playlists (should do a DANGER modal)
//  -> Websocket or something like that to provide incremental feedback and partial failures
//  -> Also refresh playlists rows
//  -> Get all pages