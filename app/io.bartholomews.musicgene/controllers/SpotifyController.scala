package io.bartholomews.musicgene.controllers

import cats.data.EitherT
import cats.effect.{ContextShift, IO}
import com.google.inject.Inject
import io.bartholomews.fsclient.entities.oauth.{AuthorizationCode, SignerV2}
import io.bartholomews.fsclient.utils.HttpTypes.HttpResponse
import io.bartholomews.musicgene.controllers.http.session.SpotifySessionUser
import io.bartholomews.musicgene.controllers.http.{SpotifyCookies, SpotifySessionKeys}
import io.bartholomews.musicgene.model.genetic.GA
import io.bartholomews.musicgene.model.music._
import io.bartholomews.musicgene.model.spotify.SpotifyService
import io.bartholomews.spotify4s.api.SpotifyApi.{Offset, SpotifyUris}
import io.bartholomews.spotify4s.entities._
import javax.inject._
import play.api.Logging
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import views.spotify.requests.{PlaylistMigrationRequest, PlaylistRequest, PlaylistsMigrationRequest, PlaylistsUnfollowRequest}
import views.spotify.responses._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

/**
 *
 */
@Singleton
class SpotifyController @Inject() (cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends AbstractControllerIO(cc)
    with Logging
    with play.api.i18n.I18nSupport {

  import cats.implicits._
  import eu.timepit.refined.auto.autoRefineV


  import io.bartholomews.musicgene.controllers.http.SpotifyHttpResults._
  import io.bartholomews.musicgene.model.helpers.CollectionsHelpers._

  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val spotifyService: SpotifyService[IO] = new SpotifyService()

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
      .map { session: SpotifySessionUser =>
        println(session.entryName)
        redirect(
          spotifyService.authorizeUrl(
            scheme = requestScheme(request),
            host = requestHost(request),
            session
          )
        )
      }
      .getOrElse(InternalServerError("Something went wrong handling spotify session, please contact support."))
  }

  def hello(): Action[AnyContent] = ActionIO.asyncWithMainUser { implicit request =>
    withToken { signer =>
      logger.info("hello")
      spotifyService.me(signer)
        .map(_.toResult(me => Ok(views.html.spotify.hello(me))))
    }
  }

  def callback(session: SpotifySessionUser): Action[AnyContent] = ActionIO.asyncWithSession(session) {
    implicit request =>
      (for {
        _ <- EitherT.pure[IO, String](logger.debug(s"Callback for session: $session"))
        uri = requestUri(request)
        authorizationCode <- EitherT(
          spotifyService.client.auth.AuthorizationCode.fromUri(uri).map(_.entity.leftMap(errorToString))
        )
      } yield Redirect(
        session match {
          case SpotifySessionUser.Main   => routes.SpotifyController.hello()
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
        spotifyService.client.auth.AuthorizationCode
          .refresh(token)
          .flatMap(_.toResultF[IO] { authorizationCode =>
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
        val pageLimit: SimplePlaylist.Limit = 50
        val pageOffset: Offset = (page - 1) * pageLimit.value
        spotifyService.client.users
          .getPlaylists(limit = pageLimit, offset = pageOffset)(accessToken)
          .map(_.toResult(pg => Ok(views.html.spotify.playlists("Playlists", pg.items, page))))
      }
    }

  def tracks(session: SpotifySessionUser, playlistId: SpotifyId): Action[AnyContent] =
    ActionIO.asyncWithSession(session) { implicit request =>
      withToken { implicit accessToken =>
        spotifyService.client.playlists
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
            spotifyService.client.tracks
              .getTracks(xs, market = None)
              .map(_.entity.leftMap(errorToJsonResult))
          )
          .parSequence
          .map(_.sequence.map(_.flatten))

      val getAudioFeatures: IO[Either[Result, List[AudioFeatures]]] =
        playlistRequest.tracks
          .groupedNes(size = 100)
          .map(xs =>
            spotifyService.client.tracks
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

  private def getSourceUserForMigration(
    mainUser: (PrivateUser, Page[SimplePlaylist])
  )(implicit request: Request[AnyContent]): IO[Result] =
    withSourceUserToken { implicit signer =>
      logger.info("getSourceUserForMigration")
      spotifyService.getUserAndPlaylists.map(
        _.toResulto(entity => Ok(views.html.spotify.migrate(Right(Tuple2(mainUser, Some(entity))))))
      )
    }.map(_.getOrElse(Ok(views.html.spotify.migrate(Right(Tuple2(mainUser, None))))))

  private def getMainUserAndPlaylists(
    andThen: ((PrivateUser, Page[SimplePlaylist])) => IO[Result]
  )(implicit request: Request[AnyContent]): IO[Result] =
    withToken { implicit signer =>
      spotifyService.getUserAndPlaylists.flatMap(
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

  def addTracksToPlaylist(
    playlistResult: HttpResponse[FullPlaylist],
    tracks: SpotifyUris
  )(implicit request: Request[AnyContent], signer: SignerV2): IO[Either[String, Result]] =
//    playlistResult.toResultEither[IO] { playlist =>
  playlistResult.entity.fold(
    error => IO.pure(Left(errorToString(error))),
    playlist =>
      spotifyService.client.playlists
        .addTracksToPlaylist(
          playlistId = playlist.id,
          uris = tracks, // FIXME: Need to get ALL pages...
          position = None
        )
        .map(_.entity.bimap(
          error => error.toString, // FIXME
          _ => Ok("Playlist have been migrated."))
        )
  )
//    }

  private def clonePlaylist(
    mainUserId: SpotifyUserId,
    playlistRequest: PlaylistMigrationRequest,
    playlistToClone: FullPlaylist
  )(implicit request: Request[AnyContent], signer: SignerV2): IO[Either[String, Result]] =
    spotifyService.client.playlists
      .createPlaylist(
        userId = mainUserId,
        playlistName = playlistRequest.name,
        public = playlistRequest.public,
        collaborative = playlistRequest.collaborative,
        description = playlistRequest.description
      )
      .flatMap { (newPlaylistResponse: HttpResponse[FullPlaylist]) =>
        SpotifyUri
          .fromList(
            playlistToClone.tracks.items
              .filterNot(_.isLocal) // FIXME: :( maybe should add to the response to inform the user
              .map(_.track.uri)
          )
          .fold(
            error => IO.pure(Left(error)),
            uris => addTracksToPlaylist(newPlaylistResponse, uris)
          )
      }

  def unfollowPlaylistsPPP(mainUserId: SpotifyUserId, playlistsToUnfollow: List[SpotifyId])(
    implicit request: Request[AnyContent]
  ): IO[Result] =
    withToken { implicit token =>
      playlistsToUnfollow
        .map(spotifyService.client.follow.unfollowPlaylist)
        .parSequence
        .map { (re: List[HttpResponse[Unit]]) =>
          val (a, b) = re.partition(result => result.status.isSuccess)
          Ok(
            Json.obj(
              ("success", a.size),
              ("failures", b.size)
            )
          )
        }
    }

  // fields = "tracks(total,previous,next,limit,offset,items(track(id)))"
  // FIXME: Generic and add to spotify4s
  def getAllOffsetToFetch[A](page: Page[A], limit: Int = 100)(implicit signer: SignerV2): List[Int] = {
    @tailrec
    def offsets(curr: Int, acc: List[Int]): List[Int] =
      if (curr >= page.total) acc
      else offsets(curr + limit, curr :: acc)
    offsets(page.items.length, Nil)
  }

  private def migratePlaylist(
    mainUserId: SpotifyUserId,
    playlistRequest: PlaylistMigrationRequest,
    mainUserToken: SignerV2,
    sourceUserToken: SignerV2
  )(implicit request: Request[AnyContent]): IO[Either[String, Result]] =
    (for {
      // FIXME: Should probably use getPlaylistFields(/tracks)
      sourceUserPlaylist <- EitherT(spotifyService.client.playlists
        .getPlaylist(playlistId = playlistRequest.id, market = None)(sourceUserToken)
        .map(_.entity.leftMap(errorToString)))

      res <- EitherT(clonePlaylist(mainUserId, playlistRequest, sourceUserPlaylist)(request, mainUserToken))
    } yield res).value

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

        unfollowPlaylistsPPP(playlistRequests.userId, playlistRequests.playlists.map(_.id))(
          request.map(AnyContent.apply)
        )
      }
    )
  }

  // Would be nice to give feedback while loading spinner, e.g. "Migrating playlist xxx..."
  // https://www.playframework.com/documentation/2.8.x/ScalaWebSockets
  val migratePlaylists: Action[JsValue] = ActionIO.jsonAsyncWithMainUser { request: Request[JsValue] =>

    implicit val anyContentRequest: Request[AnyContent] = request.map(AnyContent.apply)
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

        withToken { mainUserToken =>

        withSourceUserToken { sourceUserToken =>

          logger.debug(s"user => ${playlistRequests.userId.toString}")
          playlistRequests.playlists.foreach { pr =>
            logger.debug(pr.toString)
          }

          playlistRequests.playlists
            .map { p =>
              migratePlaylist(
                playlistRequests.userId,
                p,
                mainUserToken,
                sourceUserToken
              )(request.map(AnyContent.apply))
            }
            .sequence
            .map { re =>
              // Ior?
              val (a, b) = re.partition(result => result.isRight)
              Ok(
                Json.obj(
                  ("success", a.size),
                  ("failures", b.size)
                )
              )
            }
        }.map(_.getOrElse(
          BadRequest("Empty WAT ?")
        ))
        }
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
